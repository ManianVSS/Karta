package framework;

public class TestJava
{

   public static void main( String[] args )
   {
      float val1 = 0.9f;
      float val2 = 0.1f;

      float tocover = 1.0f;

      tocover = tocover - val1;
      System.out.println( tocover );
      tocover = tocover - val2;
      System.out.println( tocover );
      System.out.println( (float) ( ( (long) ( tocover * 1000000.f ) ) / 1000000l ) );
   }

}
