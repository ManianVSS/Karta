package org.mvss.karta.framework.core;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * A generic pair of objects
 * 
 * @author Manian
 * @param <K>
 * @param <V>
 */
@Getter
@Setter
@ToString
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Pair<K, V>
{
   private K left;
   private V right;
}
