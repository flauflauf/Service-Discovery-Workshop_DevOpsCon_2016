package de.wps.dot.filmprogramm;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import com.ecwid.consul.v1.ConsulClient;
import com.ecwid.consul.v1.agent.model.NewService;

public class MicroFilmprogrammService {

	public static void main(String[] args) {
		

		if (args.length < 1) {
			System.out.println("missing command-line argument: port");
			return;
		}

		int port = Integer.parseInt(args[0]);

		new MicroFilmprogrammService().start(port);
	}

	private List<Vorführung> ladeVorführungen() {
		return Arrays.asList(//
				new Vorführung("Filmy McFilmface", "grosser_saal", LocalDateTime.now().withHour(14).withMinute(0)), //
				new Vorführung("Filmy McFilmface", "kleiner_saal", LocalDateTime.now().withHour(16).withMinute(0)), //
				new Vorführung("Filmy McFilmface", "kleiner_saal", LocalDateTime.now().withHour(19).withMinute(0)));
	}

	private WebFilmprogrammService service;

	public MicroFilmprogrammService() {
		service = new WebFilmprogrammService(ladeVorführungen());
	}

	public void start(int port) {
		service.start(port);
		// Umlaute werden beim DNS nicht unterstützt
		
		NewService newService = new NewService();
		newService.setName("Filmprogramm");
		newService.setId("filmprogramm");
		newService.setPort(port);
		
		ConsulClient consul = new ConsulClient();
		consul.agentServiceRegister(newService);
	}

	public void stop() {
		service.stop();
	}

}