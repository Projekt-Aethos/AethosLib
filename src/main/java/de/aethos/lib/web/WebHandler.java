package de.aethos.lib.web;

import java.io.IOException;

@FunctionalInterface
public interface WebHandler {
    WebResponse handle(WebRequest request) throws IOException;
}
