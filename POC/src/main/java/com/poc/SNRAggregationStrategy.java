package com.poc;

import java.io.ByteArrayInputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.camel.Exchange;
import org.apache.camel.impl.DefaultExchange;
import org.apache.camel.processor.aggregate.AggregationStrategy;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class SNRAggregationStrategy implements AggregationStrategy {
	
	
	int noOfElementsTillNow = 0;

	@Override
	public Exchange aggregate(Exchange oldExchange, Exchange newExchange) {

		try {

			noOfElementsTillNow++;
			
			DocumentBuilderFactory domFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder builder = domFactory.newDocumentBuilder();
			
			Document newDoc = builder
					.parse(new ByteArrayInputStream(newExchange.getIn().getBody(String.class).getBytes("UTF-8")));
	
		
			int noOfElements = Integer.parseInt(newDoc.getElementsByTagName("ZAEHLER").item(0).getAttributes().getNamedItem("value").getNodeValue());
									
			if (oldExchange == null)
			{
				Exchange exchange = new DefaultExchange(newExchange);
					
				if (noOfElements == 1)
					exchange.getIn().setHeader("aggregated", true);
				else
				{
					exchange.setProperty("aggregated", false);					
				}
				
	            exchange.getIn().setBody(newExchange.getIn().getBody(String.class));
	            return exchange;
			}

			
			Document dDoc = builder
					.parse(new ByteArrayInputStream(oldExchange.getIn().getBody(String.class).getBytes("UTF-8")));

			NodeList nodes = dDoc.getElementsByTagName("SNRSATZ");

			
			Node snrListElement = newDoc.getElementsByTagName("SNRSTAMM").item(0);
			if (snrListElement != null) {
				for (int i = 0; i < nodes.getLength(); i++) {
							
					snrListElement.appendChild(newDoc.importNode(nodes.item(i), true));
				}
			}
			
			newExchange.getIn().setBody(newDoc);
			
			
			if(noOfElementsTillNow == noOfElements)
			{
				newExchange.setProperty("aggregated", true);
				noOfElementsTillNow = 0;
			}
			else		
			{
				newExchange.setProperty("aggregated", false);
			}
			
			System.out.println("New Message >> " + newExchange.getIn().getBody(String.class));

			return newExchange;

		} catch (Exception ex) {
			return null;
		}
	}

}
