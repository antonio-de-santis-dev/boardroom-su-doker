package main.service;

public record InlineAttachment(String contentId, byte[] data, String contentType) {}
