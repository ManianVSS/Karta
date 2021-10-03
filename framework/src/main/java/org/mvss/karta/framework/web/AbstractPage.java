package org.mvss.karta.framework.web;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public abstract class AbstractPage implements AutoCloseable
{
   protected WebDriverWrapper driver;
   protected WebAUT           webAUT;

   public AbstractPage( WebAUT webAUT ) throws PageException
   {
      this.webAUT = webAUT;
      this.webAUT.kartaRuntime.initializeObject( this );
      this.driver = webAUT.getDriver();
      driver.initElements( this );

      if ( !validate() )
      {
         throw new PageException( "Validation for " + this.getClass().getName() + " failed." );
      }

      this.webAUT.currentPage = this;
   }

   public abstract boolean validate();

   /**
    * Override this implementation to do clean up activities like logout etc
    */
   @Override
   public void close() throws PageException
   {

   }
}
