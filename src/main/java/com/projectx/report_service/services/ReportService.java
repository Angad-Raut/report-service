package com.projectx.report_service.services;

import com.projectx.report_service.exceptions.ResourceNotFoundException;
import com.projectx.report_service.payloads.*;

import java.text.ParseException;
import java.util.List;

public interface ReportService {
    byte[] generateReport(TwoDatesDto dto) throws ResourceNotFoundException, ParseException;
    SingleReportDto generateReportByExpenseId(EntityIdDto dto)
            throws ResourceNotFoundException,ParseException;
    List<ViewReportDto> getExpenseReportData(TwoDatesDto dto) throws ParseException;
    List<ViewReportDto> getMonthReportData(MonthRequestDto dto)throws ParseException;
    byte[] generateMonthReport(MonthRequestDto dto)throws ResourceNotFoundException, ParseException;
}
