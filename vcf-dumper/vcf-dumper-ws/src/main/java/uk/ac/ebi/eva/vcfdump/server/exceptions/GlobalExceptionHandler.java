/*
 * Copyright 2018 EMBL - European Bioinformatics Institute
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package uk.ac.ebi.eva.vcfdump.server.exceptions;

import htsjdk.samtools.util.RuntimeIOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.async.AsyncRequestTimeoutException;

import uk.ac.ebi.eva.vcfdump.VariantExporter;
import uk.ac.ebi.eva.vcfdump.server.rest.VcfDumperWSServer;

/**
 * Class to return a custom response (in the web service set in "@ControllerAdvice") in case of failure
 */
@ControllerAdvice(assignableTypes = VcfDumperWSServer.class)
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(VariantExporter.class);

    @ExceptionHandler({RuntimeException.class, RuntimeIOException.class})
    public final ResponseEntity<ApiError> handleException(Exception e) {
        HttpHeaders headers = new HttpHeaders();

        if (e instanceof AsyncRequestTimeoutException) {
            HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
            AsyncRequestTimeoutException asyncRequestTimeoutException = (AsyncRequestTimeoutException) e;
            return handleAsyncRequestTimeoutException(asyncRequestTimeoutException, headers, status);
        } else {
            HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
            return handleException(e, headers, status);
        }
    }

    private ResponseEntity<ApiError> handleAsyncRequestTimeoutException(AsyncRequestTimeoutException e,
                                                                        HttpHeaders headers, HttpStatus status) {
        logger.error("Caught timeout exception: {}", e.getMessage());
        return new ResponseEntity<>(new ApiError(e.toString(), "Timeout"), headers, status);
    }

    private ResponseEntity<ApiError> handleException(Exception e, HttpHeaders headers, HttpStatus status) {
        logger.error("Caught generic exception: {}", e.getMessage());
        return new ResponseEntity<>(new ApiError(e.getMessage(), e.getCause().getMessage()), headers, status);
    }

    static class ApiError {

        private String message;
        private String cause;

        ApiError(String message, String cause) {
            this.message = message;
            this.cause = cause;
        }

        public String getMessage() {
            return message;
        }

        public String getCause() {
            return cause;
        }
    }
}
