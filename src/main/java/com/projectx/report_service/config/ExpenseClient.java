package com.projectx.report_service.config;

import com.projectx.report_service.payloads.DateRangeDto;
import com.projectx.report_service.payloads.EntityIdDto;
import com.projectx.report_service.payloads.ExpenseInfoDto;
import com.projectx.report_service.payloads.ResponseDto;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import jakarta.validation.Valid;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

@FeignClient(value = "expense-service", url = "http://localhost:1992")
public interface ExpenseClient {

    @PostMapping(value = "/expenses/getAllExpensesWithDates")
    @CircuitBreaker(name = "expense-service",fallbackMethod = "getDummyAllExpensesWithDates")
    public ResponseEntity<ResponseDto<List<ExpenseInfoDto>>> getAllExpensesWithDates(
            @Valid @RequestBody DateRangeDto dto);

    @PostMapping(value = "/expenses/getExpenseInfoById")
    @CircuitBreaker(name = "expense-service",fallbackMethod = "getDummyGetExpenseInfoById")
    public ResponseEntity<ResponseDto<ExpenseInfoDto>> getExpenseInfoById(
            @Valid @RequestBody EntityIdDto dto);

    public default ResponseEntity<ResponseDto<List<ExpenseInfoDto>>> getDummyAllExpensesWithDates(Throwable throwable){
        String message = "Expense Service temporary down wait for sometimes!!";
        return new ResponseEntity<>(new ResponseDto<>(null,message,null), HttpStatus.OK);
    }

    public default ResponseEntity<ResponseDto<ExpenseInfoDto>> getDummyGetExpenseInfoById(Throwable throwable){
        String message = "Expense Service temporary down wait for sometimes!!";
        return new ResponseEntity<>(new ResponseDto<>(null,message,null), HttpStatus.OK);
    }
}
