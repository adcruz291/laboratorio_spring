package com.miagenda.web;

/** Cuerpo de error, mismo formato que FastAPI: {"detail": "..."}. */
public record ApiError(String detail) {
}
