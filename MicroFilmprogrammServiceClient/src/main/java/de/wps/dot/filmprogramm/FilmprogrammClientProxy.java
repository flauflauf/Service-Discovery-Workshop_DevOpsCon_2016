package de.wps.dot.filmprogramm;

import java.lang.reflect.Type;
import java.net.URI;
import java.util.List;

import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.InitialDirContext;

import com.ecwid.consul.v1.ConsulClient;
import com.ecwid.consul.v1.QueryParams;
import com.ecwid.consul.v1.Response;
import com.ecwid.consul.v1.catalog.model.CatalogService;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;

public class FilmprogrammClientProxy {
	private static Gson gson = new Gson();

	public List<Vorführung> getVorführungen() throws UnirestException, NamingException {
		List<Vorführung> vorführungen = null;

		ConsulClient consul = new ConsulClient();
		Response<List<CatalogService>> catalogService = consul.getCatalogService("filmprogramm",
				new QueryParams("dc1"));
		List<CatalogService> serviceListe = catalogService.getValue();
		CatalogService serviceInstance = serviceListe.get((int) (Math.random() * serviceListe.size()));

		String ip = serviceInstance.getAddress();

		URI uri = URI.create("http://" + ip + ":50000/vorführungen");

		HttpResponse<String> response = Unirest.get(uri.toString()).asString();
		Type vorführungsListenTyp = new TypeToken<List<Vorführung>>() {
		}.getType();
		vorführungen = gson.fromJson(response.getBody(), vorführungsListenTyp);

		return vorführungen;
	}

	private String auflösenPerDNS() throws NamingException {
		InitialDirContext kontext = DnsHelfer.erzeugeKontext();
		Attributes aAttributes = kontext.getAttributes("filmprogramm.service.consul", new String[] { "A" });
		Attribute aAttribute = aAttributes.get("A");

		String ip = DnsHelfer.findeIpAusAEintrag(aAttribute.toString());
		return ip;
	}
}
