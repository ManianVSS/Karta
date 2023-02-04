package org.mvss.karta.framework.web;

import lombok.*;

import java.io.Serializable;

@Getter
@Setter
@ToString
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class ScreenSize implements Serializable {
    private static final long serialVersionUID = 1L;

    @Builder.Default
    private boolean maximized = false;

    @Builder.Default
    private boolean fullscreen = false;

    @Builder.Default
    private int width = 1920;

    @Builder.Default
    private int height = 1080;
}
