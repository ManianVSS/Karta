package org.mvss.karta.framework.core;

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
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SerializableKVP<K extends Serializable, V extends Serializable> implements Serializable
{
   /**
    * 
    */
   private static final long serialVersionUID = 1L;

   private K                 key;
   private V                 value;
}
