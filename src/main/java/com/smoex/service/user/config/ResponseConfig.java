package com.smoex.service.user.config;

import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

import java.util.HashMap;
import java.util.Map;

@ControllerAdvice
public class ResponseConfig implements ResponseBodyAdvice {

    @ResponseBody
    @ExceptionHandler(value = Exception.class)
    public Map<String, Object> errorHandler(Exception ex) {
        return this.createFailureBody(ex.getMessage());
    }

//    @ResponseBody
//    @ExceptionHandler(value = NoHandlerFoundException.class)
//    public Map<String, Object> notFoundHandler(NoHandlerFoundException ex) {
//        return this.createFailureBody(ex.getMessage());
//    }

    @Override
    public boolean supports(MethodParameter methodParameter, Class cls) {
        return true;
    }

    @Override
    public Object beforeBodyWrite(Object o, MethodParameter methodParameter, MediaType mediaType, Class aClass, ServerHttpRequest serverHttpRequest, ServerHttpResponse serverHttpResponse) {
        if (o instanceof Map) {
            Map<String, Object> map = (Map) o;
            int code = (int) map.getOrDefault("code", -1);
            if (code != 0) {
                return o;
            }
        }
        return this.createSuccessBody(o);
    }

    private Map<String, Object> createResponseBody(Object o, int code) {
        Map<String, Object> body = new HashMap<>();
        body.put("code", code);
        if (o != null) {
            body.put("data", o);
        }
        return body;
    }
    private Map<String, Object> createSuccessBody(Object o) {
        return this.createResponseBody(o, 0);
    }
    private Map<String, Object> createFailureBody(Object o) {
        return this.createResponseBody(o, -1);
    }

}
