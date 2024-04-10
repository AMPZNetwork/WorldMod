package org.comroid.mcsd.core;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import org.comroid.api.attr.LongAttribute;
import org.comroid.api.attr.Named;
import org.comroid.api.func.util.DelegateStream;
import org.comroid.api.info.Log;
import org.comroid.api.java.ResourceLoader;
import org.comroid.mcsd.core.entity.AbstractEntity;
import org.comroid.mcsd.core.exception.CommandStatusError;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.NestedServletException;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.logging.Level;
import java.util.stream.Collectors;

@Controller
@RequestMapping
public class BasicController implements org.springframework.boot.web.servlet.error.ErrorController {
    @ResponseBody
    @GetMapping("/api/webapp/permissions")
    public Map<@NotNull Long, String> permissions() {
        return Arrays.stream(AbstractEntity.Permission.values())
                .collect(Collectors.toMap(LongAttribute::getAsLong, Named::getName));
    }

    @ResponseBody
    @GetMapping("/api/open/info/{name}")
    public String info(@PathVariable("name") String name) {
        return DelegateStream.readAll(ResourceLoader.SYSTEM_CLASS_LOADER.getResource("info/"+name+".txt"));
    }

    @GetMapping("/error")
    public String error(Model model, HttpServletRequest request) {
        var ex = (Throwable) request.getAttribute(RequestDispatcher.ERROR_EXCEPTION);
        int code = (int) Objects.requireNonNullElse(request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE),500);
        var uri = Optional.ofNullable(request.getAttribute(RequestDispatcher.ERROR_REQUEST_URI)).map(Object::toString).orElse(null);
        var msg = Optional.ofNullable(request.getAttribute(RequestDispatcher.ERROR_MESSAGE)).map(Object::toString).orElse(null);
        model.addAttribute("error", ErrorInfo.create(ex,code,uri,msg));
        return "error";
    }

    @ExceptionHandler({ Throwable.class })
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ResponseEntity<ErrorInfo> handleError(final Throwable t) {
        Log.at(Level.FINE, "Internal Server Error", t);
        return new ResponseEntity<>(ErrorInfo.create(t,500,"Internal Server Error",null), HttpStatusCode.valueOf(500));
    }

    public record ErrorInfo(String code, String message, String stacktrace) {
        public static ErrorInfo create(Throwable ex, int code, @Nullable String message, @Nullable String requestUri) {
            if (ex instanceof CommandStatusError)
                throw ((CommandStatusError) ex).toStatusCodeExc();
            var sw = new StringWriter();
            var pw = new PrintWriter(sw);
            //noinspection deprecation
            if (ex instanceof NestedServletException) {
                ex = ex.getCause();
                ex.printStackTrace(pw);
            }
            String codeMessage = code + " - ";
            HttpStatus status = HttpStatus.resolve(code);
            if (status == null)
                codeMessage += "Internal Server Error";
            else codeMessage += status.getReasonPhrase();
            if (code == 404)
                codeMessage += requestUri;
            return new ErrorInfo(codeMessage, message, sw.toString().replace("\r\n", "\n"));
        }
    }
}
