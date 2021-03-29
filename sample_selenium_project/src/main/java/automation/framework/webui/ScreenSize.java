package automation.framework.webui;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@Builder( toBuilder = true )
@NoArgsConstructor
@AllArgsConstructor
public class ScreenSize implements Serializable
{
   private static final long serialVersionUID = 1L;

   @Builder.Default
   private boolean           maximized        = true;

   @Builder.Default
   private boolean           fullscreen       = false;

   @Builder.Default
   private int               width            = 1024;

   @Builder.Default
   private int               height           = 768;
}
