package org.mvss.karta.framework.core;

import lombok.*;

/**
 * A generic pair of objects
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
public class Pair<K, V>
{
   private K left;
   private V right;
}
