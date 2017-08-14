/*
 * Copyright 2000-2014 Vaadin Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package org.vaadin.grid.enhancements.filter;

import java.util.function.Function;

import com.vaadin.data.Container.Filter;
import com.vaadin.data.Item;
import com.vaadin.data.util.BeanItemContainer;

/**
 * Simple string filter for matching items whith item caption generator.
 *
 */
public final class ItemCaptionGeneratorFilter<BEANTYPE> implements Filter {

    private static final long serialVersionUID = 1L;

    final Function<BEANTYPE, String> itemCaptionGenerator;
    final String filterString;
    final boolean ignoreCase;
    final boolean onlyMatchPrefix;
    final BeanItemContainer<BEANTYPE> container;

    public ItemCaptionGeneratorFilter(final Function<BEANTYPE, String> itemCaptionGenerator, final String filterString, final boolean ignoreCase,
            final boolean onlyMatchPrefix, final BeanItemContainer<BEANTYPE> container) {
        this.itemCaptionGenerator = itemCaptionGenerator;
        this.filterString = ignoreCase ? filterString.toLowerCase() : filterString;
        this.ignoreCase = ignoreCase;
        this.onlyMatchPrefix = onlyMatchPrefix;
        this.container = container;
    }

    @Override
    public boolean passesFilter(final Object itemId, final Item item) {
        final String value = this.itemCaptionGenerator.apply((BEANTYPE) itemId);
        if (this.onlyMatchPrefix) {
            if (!value.startsWith(this.filterString)) {
                return false;
            }
        }
        else {
            if (!value.contains(this.filterString)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean appliesToProperty(final Object propertyId) {
        return true;
    }

    @Override
    public boolean equals(final Object obj) {
        if (obj == null) {
            return false;
        }

        // Only ones of the objects of the same class can be equal
        if (!(obj instanceof ItemCaptionGeneratorFilter)) {
            return false;
        }
        @SuppressWarnings("unchecked")
        final ItemCaptionGeneratorFilter<BEANTYPE> o = (ItemCaptionGeneratorFilter<BEANTYPE>) obj;

        // Checks the properties one by one
        if (this.filterString != o.filterString && o.filterString != null && !o.filterString.equals(this.filterString)) {
            return false;
        }
        if (this.ignoreCase != o.ignoreCase) {
            return false;
        }
        if (this.onlyMatchPrefix != o.onlyMatchPrefix) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        return (this.filterString != null ? this.filterString.hashCode() : 0);
    }

    /**
     * Returns the filter string.
     *
     * Note: this method is intended only for implementations of lazy string filters and may change in the future.
     *
     * @return filter string given to the constructor
     */
    public String getFilterString() {
        return this.filterString;
    }

    /**
     * Returns whether the filter is case-insensitive or case-sensitive.
     *
     * Note: this method is intended only for implementations of lazy string filters and may change in the future.
     *
     * @return true if performing case-insensitive filtering, false for case-sensitive
     */
    public boolean isIgnoreCase() {
        return this.ignoreCase;
    }

    /**
     * Returns true if the filter only applies to the beginning of the value string, false for any location in the value.
     *
     * Note: this method is intended only for implementations of lazy string filters and may change in the future.
     *
     * @return true if checking for matches at the beginning of the value only, false if matching any part of value
     */
    public boolean isOnlyMatchPrefix() {
        return this.onlyMatchPrefix;
    }
}
