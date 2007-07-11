package de.ingrid.mdek;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import net.weta.components.communication.ICommunication;
import net.weta.components.communication.WetagURL;
import net.weta.components.communication.reflect.ProxyService;
import net.weta.components.communication.tcp.StartCommunication;
import net.weta.components.communication.tcp.TcpCommunication;
import de.ingrid.mdek.job.repository.IJobRepositoryFacade;

public class MdekClient {

    private static MdekClient _client = null;

    private static IJobRepositoryFacade _repository;

    public MdekClient() {
        // private
    }

    public static MdekClient getInstance(File communicationProperties) throws IOException {
        if (_client == null) {
            _client = new MdekClient();
            _repository = _client.startup(communicationProperties);
        }
        return _client;
    }

    public IJobRepositoryFacade getJobRepositoryFacade() {
        return _repository;
    }

    private IJobRepositoryFacade startup(File communicationProperties) throws IOException {
        ICommunication communication = initCommunication(communicationProperties);

        String proxyServiceUrl = null;
        if (communication instanceof TcpCommunication) {
            TcpCommunication tcpComm = (TcpCommunication) communication;
            proxyServiceUrl = (String) tcpComm.getServerNames().get(0);
        }
        IJobRepositoryFacade repository = (IJobRepositoryFacade) ProxyService.createProxy(communication,
                IJobRepositoryFacade.class, proxyServiceUrl);

        return repository;
    }

    private ICommunication initCommunication(File properties) throws IOException {
        FileInputStream confIS = new FileInputStream(properties);
        ICommunication communication = StartCommunication.create(confIS);
        communication.startup();

        return communication;
    }
}
