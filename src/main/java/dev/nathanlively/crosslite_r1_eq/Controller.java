package dev.nathanlively.crosslite_r1_eq;

import org.springframework.shell.command.annotation.Command;

@Command
public class Controller {
    private final FileConversionService fileConversionService;

    public Controller(FileConversionService fileConversionService) {
        this.fileConversionService = fileConversionService;
    }
}
