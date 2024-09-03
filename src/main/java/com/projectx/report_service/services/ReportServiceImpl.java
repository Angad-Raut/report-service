package com.projectx.report_service.services;

import com.projectx.report_service.config.ExpenseClient;
import com.projectx.report_service.exceptions.ResourceNotFoundException;
import com.projectx.report_service.payloads.*;
import com.projectx.report_service.utils.ReportUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.text.ParseException;
import java.time.LocalDateTime;
import java.time.Month;
import java.time.ZoneId;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@Component
public class ReportServiceImpl implements ReportService {

    @Autowired
    private ExpenseClient expenseClient;

    @Override
    public byte[] generateReport(TwoDatesDto dto) throws ParseException {
        byte[] byteData = null;
        Date startDate = ReportUtils.getISOStartDate(dto.getStartDate());
        Date endDate = ReportUtils.getISOEndDate(dto.getEndDate());
        AtomicInteger index = new AtomicInteger(0);
        List<ExpenseInfoDto> fetchList = getAllExpenses(startDate,endDate);
        List<ViewReportDto> reportDataList = fetchList!=null && !fetchList.isEmpty()?fetchList.stream()
                .map(data -> setExpenseData(data,index))
                .toList():new ArrayList<>();
        if (reportDataList!=null && !reportDataList.isEmpty()) {
            return ReportGenerator.generateReport(reportDataList, dto.getStartDate(), dto.getEndDate());
        } else {
            throw new ResourceNotFoundException(ReportUtils.REPORT_NOT_FOUND);
        }
    }

    @Override
    public SingleReportDto generateReportByExpenseId(EntityIdDto dto) throws ResourceNotFoundException, ParseException {
        try {
            byte[] byteData = null;
            ExpenseInfoDto details = getExpenseInfoById(dto.getEntityId());
            if (details==null) {
                throw new ResourceNotFoundException(ReportUtils.EXPENSE_NOT_EXISTS);
            }
            AtomicInteger counter = new AtomicInteger(0);
            ViewReportDto reportDto = ViewReportDto.builder()
                    .srNo(1)
                    .expenseId(details.getId())
                    .expenseDate(ReportUtils.toExpenseDate(details.getInsertedTime()))
                    .totalAmount(details.getTotalAmount()!=null?ReportUtils.toINRFormat(details.getTotalAmount()):ReportUtils.DASH)
                    .total(details.getTotalAmount()!=null?details.getTotalAmount():0.0)
                    .itemsList(details.getExpenseItems().stream()
                            .map(item -> ViewExpenseItemsDto.builder()
                                    .srNo(counter.incrementAndGet())
                                    .itemName(item.getItemName())
                                    .itemPrice(item.getItemPrice()!=null?ReportUtils.toINRFormat(item.getItemPrice()):ReportUtils.DASH)
                                    .paymentWith(item.getPaymentType())
                                    .build())
                            .toList())
                    .build();
            if (reportDto!=null) {
                byte[] data = ReportGenerator.generateReportByExpenseId(reportDto);
                return SingleReportDto.builder()
                        .byteData(data)
                        .expenseDate(reportDto.getExpenseDate())
                        .build();
            } else {
                throw new ResourceNotFoundException(ReportUtils.REPORT_NOT_FOUND);
            }
        } catch (ResourceNotFoundException e) {
            throw new ResourceNotFoundException(e.getMessage());
        }
    }

    @Override
    public List<ViewReportDto> getExpenseReportData(TwoDatesDto dto) throws ParseException {
        Date startDate = ReportUtils.getISOStartDate(dto.getStartDate());
        Date endDate = ReportUtils.getISOEndDate(dto.getEndDate());
        AtomicInteger index = new AtomicInteger(0);
        List<ExpenseInfoDto> fetchList = getAllExpenses(startDate,endDate);
        return fetchList!=null && !fetchList.isEmpty()?fetchList.stream()
                .map(data -> setExpenseData(data,index))
                .toList():new ArrayList<>();

    }

    @Override
    public List<ViewReportDto> getMonthReportData(MonthRequestDto dto) throws ParseException {
        byte[] byteData = null;
        String data[] = dto.getMonthName().split(" ");
        LocalDateTime localDateTime = LocalDateTime.of(Integer.parseInt(data[1].toString()), Month.valueOf(data[0].toString().toUpperCase()),1,0,0,0,0);
        LocalDateTime startDate = localDateTime.with(TemporalAdjusters.firstDayOfMonth()).plusHours(0).plusMinutes(0).plusSeconds(0);
        LocalDateTime endDate = localDateTime.with(TemporalAdjusters.lastDayOfMonth()).plusHours(23).plusMinutes(59).plusSeconds(59);
        Date fromDate = Date.from(startDate.atZone(ZoneId.systemDefault()).with(TemporalAdjusters.firstDayOfMonth()).toInstant());
        Date toDate = Date.from(endDate.atZone(ZoneId.systemDefault()).with(TemporalAdjusters.lastDayOfMonth()).toInstant());
        AtomicInteger index = new AtomicInteger(0);
        List<ExpenseInfoDto> fetchList = getAllExpenses(fromDate,toDate);
        List<ViewReportDto> reportDataList = fetchList!=null && !fetchList.isEmpty()?fetchList.stream()
                .map(result -> setExpenseData(result,index))
                .toList():new ArrayList<>();
        return reportDataList;
    }

    @Override
    public byte[] generateMonthReport(MonthRequestDto dto) throws ParseException {
        byte[] byteData = null;
        List<ViewReportDto> reportDataList = getMonthReportData(dto);
        if (reportDataList!=null && !reportDataList.isEmpty()) {
            return ReportGenerator.generateMonthReport(reportDataList, dto.getMonthName());
        } else {
            throw new ResourceNotFoundException(ReportUtils.REPORT_NOT_FOUND);
        }
    }

    private ViewReportDto setExpenseData(ExpenseInfoDto details,AtomicInteger index) {
        AtomicInteger counter = new AtomicInteger(0);
        return ViewReportDto.builder()
                .srNo(index.incrementAndGet())
                .expenseId(details.getId())
                .itemCount(details.getExpenseItems().size())
                .expenseDate(ReportUtils.toExpenseDate(details.getInsertedTime()))
                .totalAmount(details.getTotalAmount()!=null?ReportUtils.toINRFormat(details.getTotalAmount()):ReportUtils.DASH)
                .total(details.getTotalAmount()!=null?details.getTotalAmount():0.0)
                .itemsList(details.getExpenseItems().stream()
                        .map(item -> ViewExpenseItemsDto.builder()
                                .srNo(counter.incrementAndGet())
                                .itemName(item.getItemName())
                                .itemPrice(item.getItemPrice()!=null?ReportUtils.toINRFormat(item.getItemPrice()):ReportUtils.DASH)
                                .paymentWith(item.getPaymentType())
                                .build())
                        .toList())
                .build();
    }

    private List<ExpenseInfoDto> getAllExpenses(Date startDate,Date endDate){
        DateRangeDto dateRangeDto = DateRangeDto.builder()
                .startDate(startDate)
                .endDate(endDate)
                .build();
        ResponseEntity<ResponseDto<List<ExpenseInfoDto>>> response = expenseClient.getAllExpensesWithDates(dateRangeDto);
        return response.getBody().getResult();
    }

    private ExpenseInfoDto getExpenseInfoById(Long expenseId){
        ResponseEntity<ResponseDto<ExpenseInfoDto>> response = expenseClient.getExpenseInfoById(new EntityIdDto(expenseId));
        return response.getBody().getResult();
    }
}
