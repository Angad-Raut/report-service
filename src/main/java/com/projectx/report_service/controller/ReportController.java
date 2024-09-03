package com.projectx.report_service.controller;

import com.projectx.report_service.exceptions.InvalidDataException;
import com.projectx.report_service.exceptions.ResourceNotFoundException;
import com.projectx.report_service.payloads.*;
import com.projectx.report_service.services.ReportService;
import com.projectx.report_service.utils.ErrorHandlerComponent;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.config.InvalidConfigDataPropertyException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.text.ParseException;
import java.util.List;

@RestController
@RequestMapping(value = "/reports")
public class ReportController {

    @Autowired
    private ReportService reportService;

    @Autowired
    private ErrorHandlerComponent errorHandler;

    @PostMapping("/downloadReport")
    public ResponseEntity<ResponseDto<byte[]>> downloadReport(
            @Valid @RequestBody TwoDatesDto dto, BindingResult result) {
        if (result.hasErrors()){
            return errorHandler.handleValidationErrors(result);
        }
        try {
            byte[] data = reportService.generateReport(dto);
            return new ResponseEntity<>(new ResponseDto<>(
                    data,null,null), HttpStatus.OK);
        } catch (ResourceNotFoundException | InvalidConfigDataPropertyException | ParseException e) {
            return errorHandler.handleError(e);
        }
    }

    @PostMapping("/downloadReportByExpenseId")
    public ResponseEntity<ResponseDto<SingleReportDto>> downloadReportByExpenseId(
            @Valid @RequestBody EntityIdDto dto,BindingResult result) {
        if (result.hasErrors()){
            return errorHandler.handleValidationErrors(result);
        }
        try {
            SingleReportDto data = reportService.generateReportByExpenseId(dto);
            return new ResponseEntity<>(new ResponseDto<>(
                    data,null,null), HttpStatus.OK);
        } catch (ResourceNotFoundException | InvalidDataException | ParseException e) {
            return errorHandler.handleError(e);
        }
    }

    @PostMapping("/getExpenseReportData")
    public ResponseEntity<ResponseDto<List<ViewReportDto>>> getExpenseReportData(
            @Valid @RequestBody TwoDatesDto dto,BindingResult result) {
        if (result.hasErrors()){
            return errorHandler.handleValidationErrors(result);
        }
        try {
            List<ViewReportDto> data = reportService.getExpenseReportData(dto);
            return new ResponseEntity<>(new ResponseDto<>(
                    data,null,null), HttpStatus.OK);
        } catch (ResourceNotFoundException | InvalidDataException | ParseException e) {
            return errorHandler.handleError(e);
        }
    }

    @PostMapping("/getMonthReportData")
    public ResponseEntity<ResponseDto<List<ViewReportDto>>> getMonthReportData(
            @Valid @RequestBody MonthRequestDto dto,BindingResult result) {
        if (result.hasErrors()){
            return errorHandler.handleValidationErrors(result);
        }
        try {
            List<ViewReportDto> data = reportService.getMonthReportData(dto);
            return new ResponseEntity<>(new ResponseDto<>(
                    data,null,null), HttpStatus.OK);
        } catch (ResourceNotFoundException | InvalidDataException | ParseException e) {
            return errorHandler.handleError(e);
        }
    }

    @PostMapping("/generateMonthReport")
    public ResponseEntity<ResponseDto<byte[]>> generateMonthReport(
            @Valid @RequestBody MonthRequestDto dto,BindingResult result) {
        if (result.hasErrors()){
            return errorHandler.handleValidationErrors(result);
        }
        try {
            byte[] data = reportService.generateMonthReport(dto);
            return new ResponseEntity<>(new ResponseDto<>(
                    data,null,null), HttpStatus.OK);
        } catch (ResourceNotFoundException | InvalidDataException | ParseException e) {
            return errorHandler.handleError(e);
        }
    }
}
