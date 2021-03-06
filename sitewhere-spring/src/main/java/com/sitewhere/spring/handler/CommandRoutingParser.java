/*
 * Copyright (c) SiteWhere, LLC. All rights reserved. http://www.sitewhere.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package com.sitewhere.spring.handler;

import java.util.List;

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.RuntimeBeanReference;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.ManagedMap;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.util.xml.DomUtils;
import org.w3c.dom.Attr;
import org.w3c.dom.Element;

import com.sitewhere.device.communication.SpecificationMappingCommandRouter;
import com.sitewhere.groovy.device.communication.GroovyCommandRouter;
import com.sitewhere.spring.handler.ICommandRoutingParser.Elements;

/**
 * Parse elements related to command routing.
 * 
 * @author Derek
 */
public class CommandRoutingParser {

    /**
     * Parse elements contained in command routing section.
     * 
     * @param element
     * @param context
     * @return
     */
    protected Object parse(Element element, ParserContext context) {
	List<Element> children = DomUtils.getChildElements(element);
	for (Element child : children) {
	    Elements type = Elements.getByLocalName(child.getLocalName());
	    if (type == null) {
		throw new RuntimeException("Unknown command routing element: " + child.getLocalName());
	    }
	    switch (type) {
	    case CommandRouter: {
		return parseCommandRouterReference(child, context);
	    }
	    case SpecificationMappingRouter: {
		return parseSpecificationMappingRouter(child, context);
	    }
	    case GroovyCommandRouter: {
		return parseGroovyCommandRouter(child, context);
	    }
	    }
	}
	return null;
    }

    /**
     * Parse a command router reference.
     * 
     * @param element
     * @param context
     * @return
     */
    protected RuntimeBeanReference parseCommandRouterReference(Element element, ParserContext context) {
	Attr ref = element.getAttributeNode("ref");
	if (ref != null) {
	    return new RuntimeBeanReference(ref.getValue());
	}
	throw new RuntimeException("Command router reference does not have ref defined.");
    }

    /**
     * Parse the configuration for a {@link SpecificationMappingCommandRouter}.
     * 
     * @param element
     * @param context
     * @return
     */
    protected BeanDefinition parseSpecificationMappingRouter(Element element, ParserContext context) {
	BeanDefinitionBuilder router = BeanDefinitionBuilder
		.rootBeanDefinition(SpecificationMappingCommandRouter.class);

	Attr defaultDestination = element.getAttributeNode("defaultDestination");
	if (defaultDestination != null) {
	    router.addPropertyValue("defaultDestination", defaultDestination.getValue());
	}

	ManagedMap<String, String> map = new ManagedMap<String, String>();
	List<Element> mappings = DomUtils.getChildElementsByTagName(element, "mapping");
	for (Element mapping : mappings) {
	    Attr token = mapping.getAttributeNode("specification");
	    if (token == null) {
		throw new RuntimeException("Specification mapping missing specification token.");
	    }
	    Attr destination = mapping.getAttributeNode("destination");
	    if (destination == null) {
		throw new RuntimeException("Specification mapping missing destination id.");
	    }
	    map.put(token.getValue(), destination.getValue());
	}
	router.addPropertyValue("mappings", map);
	return router.getBeanDefinition();
    }

    /**
     * Parse the configuration for a {@link GroovyCommandRouter}.
     * 
     * @param element
     * @param context
     * @return
     */
    protected BeanDefinition parseGroovyCommandRouter(Element element, ParserContext context) {
	BeanDefinitionBuilder router = BeanDefinitionBuilder.rootBeanDefinition(GroovyCommandRouter.class);

	Attr scriptPath = element.getAttributeNode("scriptPath");
	if (scriptPath != null) {
	    router.addPropertyValue("scriptPath", scriptPath.getValue());
	}

	return router.getBeanDefinition();
    }
}