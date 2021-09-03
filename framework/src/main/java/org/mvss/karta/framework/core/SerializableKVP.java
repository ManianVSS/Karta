package org.mvss.karta.framework.core;

import lombok.*;

import java.io.Serializable;

/**
 * Serializable key value pair
 *
 * @param <K>
 * @param <V>
 * @author Manian
 */
@Getter
@Setter
@ToString
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SerializableKVP<K extends Serializable, V extends Serializable> implements Serializable
{
   private static final long serialVersionUID = 1L;

   private K key;
   private V value;
}
