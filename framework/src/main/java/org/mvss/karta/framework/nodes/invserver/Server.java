package org.mvss.karta.framework.nodes.invserver;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface Server extends Remote, AutoCloseable {
    boolean healthCheck() throws RemoteException;

    WorkerConfiguration registerNode(WorkerConfiguration workerConfiguration) throws RemoteException;

    boolean removeNode(WorkerConfiguration workerConfiguration) throws RemoteException;

}
