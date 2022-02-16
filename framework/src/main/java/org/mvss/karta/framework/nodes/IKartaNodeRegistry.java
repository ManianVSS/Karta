package org.mvss.karta.framework.nodes;

public interface IKartaNodeRegistry extends AutoCloseable
{
   boolean addNode( KartaNodeConfiguration nodeConfiguration );

   boolean removeNode( String name );

   KartaNode getNode( String name );

   KartaNode getNextMinion();

   java.util.ArrayList<KartaNode> getMinions();

   java.util.HashMap<String, KartaNode> getNodes();
}
