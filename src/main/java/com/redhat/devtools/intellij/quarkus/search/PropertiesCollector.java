/*******************************************************************************
* Copyright (c) 2019 Red Hat Inc. and others.
*
* This program and the accompanying materials are made available under the
* terms of the Eclipse Public License v. 2.0 which is available at
* http://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
* which is available at https://www.apache.org/licenses/LICENSE-2.0.
*
* SPDX-License-Identifier: EPL-2.0 OR Apache-2.0
*
* Contributors:
*     Red Hat Inc. - initial API and implementation
*******************************************************************************/
package com.redhat.devtools.intellij.quarkus.search;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.eclipse.lsp4mp.commons.MicroProfilePropertiesScope;
import org.eclipse.lsp4mp.commons.metadata.ConfigurationMetadata;
import org.eclipse.lsp4mp.commons.metadata.ItemHint;
import org.eclipse.lsp4mp.commons.metadata.ItemMetadata;

/**
 * Properties collector implementation.
 *
 * @author Angelo ZERR
 *
 */
public class PropertiesCollector implements IPropertiesCollector {

	private final ConfigurationMetadata configuration;

	private final Map<String, ItemHint> hintsCache;

	private final boolean onlySources;

	public PropertiesCollector(ConfigurationMetadata configuration, List<MicroProfilePropertiesScope> scopes) {
		this.configuration = configuration;
		this.configuration.setProperties(new ArrayList<>());
		this.configuration.setHints(new ArrayList<>());
		this.hintsCache = new HashMap<>();
		this.onlySources = MicroProfilePropertiesScope.isOnlySources(scopes);
	}

	@Override
	public ItemMetadata addItemMetadata(String name, String type, String description, String sourceType,
			String sourceField, String sourceMethod, String defaultValue, String extensionName, boolean binary,
			int phase) {
		ItemMetadata property = new ItemMetadata();
		property.setName(name);
		property.setType(type);
		property.setDescription(description);
		property.setSourceType(sourceType);
		property.setSourceField(sourceField);
		property.setSourceMethod(sourceMethod);
		property.setDefaultValue(defaultValue);

		// Extra properties

		property.setExtensionName(extensionName);
		if (!binary) {
			property.setSource(Boolean.TRUE);
		}
		property.setPhase(phase);
		property.setRequired(defaultValue == null);

		configuration.getProperties().add(property);
		return property;
	}

	@Override
	public boolean hasItemHint(String hint) {
		return hintsCache.containsKey(hint);
	}

	private void addItemHint(ItemHint itemHint) {
		configuration.getHints().add(itemHint);
		hintsCache.put(itemHint.getName(), itemHint);
	}

	@Override
	public ItemHint getItemHint(String hint) {
		ItemHint itemHint = hintsCache.get(hint);
		if (itemHint != null) {
			return itemHint;
		}
		itemHint = new ItemHint();
		itemHint.setName(hint);
		itemHint.setValues(new ArrayList<>());
		addItemHint(itemHint);
		return itemHint;
	}

	@Override
	public void merge(ConfigurationMetadata metadata, MergingStrategy mergingStrategy) {
		List<ItemMetadata> properties = metadata.getProperties();
		if (properties == null) {
			return;
		}
		for (ItemMetadata property: properties) {
			merge(property, mergingStrategy);
		}
		List<ItemHint> hints = metadata.getHints();
		if (hints != null) {
			for (ItemHint itemHint : hints) {
				addItemHint(itemHint);
			}
		}
	}

	public void merge(ItemMetadata property, MergingStrategy mergingStrategy) {
		if (onlySources && (property.getSource() == null || !property.getSource())) {
			// In the case of the scopes is only sources, the property which is a binary
			// property must not be added.
			return;
		}
		switch (mergingStrategy) {
			case IGNORE_IF_EXISTS:
				mergeWithIgnoreIfExists(property);
				break;
			case REPLACE:
				mergeWithReplace(property);
				break;
			default:
				addProperty(property);
				break;
		}
	}

	private void mergeWithIgnoreIfExists(ItemMetadata property) {
		Optional<ItemMetadata> configProperty = getExistingProperty(property);
		if (configProperty.isPresent()) {
			return;
		}
		addProperty(property);
	}

	private Optional<ItemMetadata> getExistingProperty(ItemMetadata property) {
		List<ItemMetadata> configProperties = configuration.getProperties();
		Optional<ItemMetadata> configProperty = configProperties.stream()
				.filter(cp -> cp.getName().equals(property.getName())).findFirst();
		return configProperty;
	}

	private void mergeWithReplace(ItemMetadata property) {
		Optional<ItemMetadata> configProperty = getExistingProperty(property);
		if (configProperty.isPresent()) {
			configuration.getProperties().remove(configProperty.get());
		}
		addProperty(property);
	}

	private void addProperty(ItemMetadata property) {
		configuration.getProperties().add(property);
	}
}
