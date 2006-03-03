/*
 * Copyright (c) 1998-2006 Caucho Technology -- all rights reserved
 *
 * This file is part of Resin(R) Open Source
 *
 * Each copy or derived work must preserve the copyright notice and this
 * notice unmodified.
 *
 * Resin Open Source is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * Resin Open Source is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE, or any warranty
 * of NON-INFRINGEMENT.  See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Resin Open Source; if not, write to the
 *   Free Software Foundation, Inc.
 *   59 Temple Place, Suite 330
 *   Boston, MA 02111-1307  USA
 *
 * @author Scott Ferguson
 */

package com.caucho.amber.query;

import java.sql.ResultSet;
import java.sql.SQLException;

import java.util.ArrayList;

import com.caucho.amber.entity.EntityItem;

import com.caucho.amber.field.AmberField;

import com.caucho.amber.manager.AmberConnection;

import com.caucho.amber.table.Table;
import com.caucho.amber.table.LinkColumns;

import com.caucho.amber.type.Type;
import com.caucho.amber.type.EntityType;

import com.caucho.util.CharBuffer;

/**
 * An entity expression which should be loaded.
 */
public class LoadEntityExpr extends AbstractAmberExpr {
  private PathExpr _expr;
  private FromItem _fromItem;

  private ArrayList<FromItem> _subItems = new ArrayList<FromItem>();

  LoadEntityExpr(PathExpr expr)
  {
    _expr = expr;
  }
  
  /**
   * Returns the type.
   */
  public Type getType()
  {
    return getEntityType();
  }
  
  /**
   * Returns the entity type.
   */
  public EntityType getEntityType()
  {
    return (EntityType) _expr.getTargetType();
  }
  
  /**
   * Returns the table.
   */
  public String getTable()
  {
    return _fromItem.getName();
  }

  /**
   * Binds the expression as a select item.
   */
  public AmberExpr bindSelect(QueryParser parser)
  {
    _fromItem = _expr.bindSubPath(parser);

    if (_fromItem == null)
      throw new NullPointerException(_expr.getClass().getName() + " " + _expr);

    EntityType type = getEntityType();

    if (type.getSecondaryTables().size() > 0) {
      for (AmberField field : type.getFields()) {
	Table subTable = field.getTable();

	if (subTable != null && subTable != type.getTable()) {
	  LinkColumns link = subTable.getDependentIdLink();

	  FromItem item = parser.createDependentFromItem(_fromItem, link);

	  _subItems.add(item);
	}
      }
    }

    return this;
  }

  /**
   * Binds the expression as a select item.
   */
  public FromItem bindSubPath(QueryParser parser)
  {
    throw new UnsupportedOperationException();
  }

  /**
   * Returns true if the expression uses the from item.
   */
  public boolean usesFrom(FromItem from, int type, boolean isNot)
  {
    if (_fromItem == from)
      return true;

    for (int i = 0; i < _subItems.size(); i++) {
      FromItem subItem = _subItems.get(i);

      if (from == subItem)
	return true;
    }
    
    return _expr.usesFrom(from, type, isNot);
  }
  
  /**
   * Returns the from item
   */
  public FromItem getChildFromItem()
  {
    return _expr.getChildFromItem();
  }
  
  /**
   * Generates the where expression.
   */
  public void generateSelect(CharBuffer cb)
  {
    cb.append(getEntityType().getId().generateSelect(getTable()));

    EntityType type = getEntityType();
    
    String valueSelect = type.generateLoadSelect(_fromItem.getTable(),
						 _fromItem.getName());

    if (valueSelect != null && ! "".equals(valueSelect)) {
      cb.append(", ");
      cb.append(valueSelect);
    }

    for (int i = 0; i < _subItems.size(); i++) {
      FromItem item = _subItems.get(i);
      
      valueSelect = type.generateLoadSelect(item.getTable(), item.getName());

      if (! valueSelect.equals("")) {
	cb.append(", ");
	cb.append(valueSelect);
      }
    }
  }
  
  /**
   * Generates the where expression.
   */
  public void generateWhere(CharBuffer cb, String fieldName)
  {
    throw new UnsupportedOperationException();
  }

  /**
   * Returns the object for the expr.
   */
  public Object getObject(AmberConnection aConn, ResultSet rs, int index)
    throws SQLException
  {
    return getEntityType().getLoadObject(aConn, rs, index);
  }

  /**
   * Returns the object for the expr.
   */
  public Object getCacheObject(AmberConnection aConn,
			       ResultSet rs, int index)
    throws SQLException
  {
    return findItem(aConn, rs, index);
  }

  /**
   * Returns the object for the expr.
   */
  public EntityItem findItem(AmberConnection aConn, ResultSet rs, int index)
    throws SQLException
  {
    EntityType entityType = getEntityType();
    
    EntityItem item = entityType.getHome().findItem(aConn, rs, index);
    
    int keyLength = entityType.getId().getKeyCount();

    item.getEntity().__caucho_load(aConn, rs, index + keyLength);

    return item;
  }
}
