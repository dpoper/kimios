/*
 * Kimios - Document Management System Software
 * Copyright (C) 2008-2015  DevLib'
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 2 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 * You should have received a copy of the GNU Affero General Public License
 * aong with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.kimios.kernel.dms.model;

import org.kimios.kernel.dms.extension.GenericAttribute;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.Transient;
import java.io.Serializable;

@Embeddable
public class DMEntityAttribute implements GenericAttribute, Serializable
{
    private boolean indexed = false;

    private String name;

    private String value;

    @Column(name = "attribute_value", nullable = true, length = 5000)
    public String getValue()
    {
        return value;
    }

    public void setValue(String value)
    {
        this.value = value;
    }

    @Column(name = "attribute_name_dbl", nullable = false)
    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public void setIndexed(boolean indexed)
    {
        this.indexed = indexed;
    }

    @Column(name = "is_indexed", nullable = false)
    public boolean isIndexed()
    {
        return indexed;
    }

    @Transient
    public org.kimios.kernel.ws.pojo.DMEntityAttribute toPojo()
    {
        org.kimios.kernel.ws.pojo.DMEntityAttribute pojo = new org.kimios.kernel.ws.pojo.DMEntityAttribute();
        pojo.setIndexed(indexed);
        pojo.setValue(value);
        pojo.setName(name);

        return pojo;
    }
}

