package com.fincatto.documentofiscal.cte.webservices.distribuicao;

import com.fincatto.documentofiscal.cte.classes.distribuicao.CTDistribuicaoInt;
import com.fincatto.documentofiscal.cte200.classes.CTAutorizador;
import com.fincatto.documentofiscal.nfe.NFeConfig;
import com.fincatto.documentofiscal.utils.DFSocketFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.util.AXIOMUtil;
import org.apache.commons.httpclient.protocol.Protocol;

import javax.xml.stream.XMLStreamException;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.rmi.RemoteException;
import java.util.Base64;
import java.util.zip.GZIPInputStream;

public class WSDistribuicaoCTe {

    /**
     * Metodo para consultar os conhecimentos de transporte e retorna uma String<br>
     * E importante salvar esta String para nao perder nenhuma informacao<br>
     * A receita nao disponibiliza o conhecimento varias vezes para consultar, retorna rejeicao: Consumo indevido
     */
    public static String consultar(final CTDistribuicaoInt distDFeInt, final NFeConfig config) throws Exception {
        Protocol.registerProtocol("https", new Protocol("https", new DFSocketFactory(config), 443));
        try {
            final OMElement ome = AXIOMUtil.stringToOM(distDFeInt.toString());

            final CTeDistribuicaoDFeSoapStub.CteDadosMsg_type0 dadosMsgType0 = new CTeDistribuicaoDFeSoapStub.CteDadosMsg_type0();
            dadosMsgType0.setExtraElement(ome);

            final CTeDistribuicaoDFeSoapStub.CteDistDFeInteresse distDFeInteresse = new CTeDistribuicaoDFeSoapStub.CteDistDFeInteresse();
            distDFeInteresse.setCteDadosMsg(dadosMsgType0);

            final CTeDistribuicaoDFeSoapStub stub = new CTeDistribuicaoDFeSoapStub(CTAutorizador.AN.getDistribuicaoDFe(config.getAmbiente()));
            final CTeDistribuicaoDFeSoapStub.CteDistDFeInteresseResponse result = stub.cteDistDFeInteresse(distDFeInteresse);

            return result.getCteDistDFeInteresseResult().getExtraElement().toString();
        } catch (RemoteException | XMLStreamException e) {
            throw new Exception(e.getMessage());
        }
    }

    public static String decodeGZipToXml(final String conteudoEncode) throws Exception {
        if (conteudoEncode == null || conteudoEncode.length() == 0) {
            return "";
        }
        final byte[] conteudo = Base64.getDecoder().decode(conteudoEncode);//java 8
        //final byte[] conteudo = DatatypeConverter.parseBase64Binary(conteudoEncode);//java 7
        try (GZIPInputStream gis = new GZIPInputStream(new ByteArrayInputStream(conteudo))) {
            try (BufferedReader bf = new BufferedReader(new InputStreamReader(gis, StandardCharsets.UTF_8))) {
                StringBuilder outStr = new StringBuilder();
                String line;
                while ((line = bf.readLine()) != null) {
                    outStr.append(line);
                }
                return outStr.toString();
            }
        }
    }
    
    //    Nao reviver este metodo. Usar o oficial:  this.config.getPersister().read(classe, xml)
    //    public static <T> T xmlToObject(final String xml, final Class<T> classe) throws Exception {
    //        return new Persister(new DFRegistryMatcher(TimeZone.getDefault()), new Format(0)).read(classe, xml);
    //    }
}