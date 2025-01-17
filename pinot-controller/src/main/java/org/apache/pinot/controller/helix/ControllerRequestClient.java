/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.pinot.controller.helix;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import org.apache.pinot.common.exception.HttpErrorStatusException;
import org.apache.pinot.common.utils.SimpleHttpResponse;
import org.apache.pinot.common.utils.http.HttpClient;
import org.apache.pinot.spi.config.table.TableConfig;
import org.apache.pinot.spi.config.table.TableType;
import org.apache.pinot.spi.config.tenant.Tenant;
import org.apache.pinot.spi.config.tenant.TenantRole;
import org.apache.pinot.spi.data.Schema;
import org.apache.pinot.spi.utils.JsonUtils;


/**
 * The {@code ControllerRequestClient} provides handy utilities to make request to controller.
 *
 * <p>It should be provided with a specified {@link ControllerRequestURLBuilder} for constructing the URL requests
 * as well as a reusable {@link HttpClient} during construction.
 */
public class ControllerRequestClient {
  private final HttpClient _httpClient;
  private final ControllerRequestURLBuilder _controllerRequestURLBuilder;

  public ControllerRequestClient(ControllerRequestURLBuilder controllerRequestUrlBuilder, HttpClient httpClient) {
    _controllerRequestURLBuilder = controllerRequestUrlBuilder;
    _httpClient = httpClient;
  }

  public ControllerRequestURLBuilder getControllerRequestURLBuilder() {
    return _controllerRequestURLBuilder;
  }
  /**
   * Add a schema to the controller.
   */
  public void addSchema(Schema schema)
      throws IOException {
    String url = _controllerRequestURLBuilder.forSchemaCreate();
    try {
      HttpClient.wrapAndThrowHttpException(_httpClient.sendMultipartPostRequest(url, schema.toSingleLineJsonString()));
    } catch (HttpErrorStatusException e) {
      throw new IOException(e);
    }
  }

  public Schema getSchema(String schemaName)
      throws IOException {
    String url = _controllerRequestURLBuilder.forSchemaGet(schemaName);
    try {
      SimpleHttpResponse resp = HttpClient.wrapAndThrowHttpException(_httpClient.sendGetRequest(new URL(url).toURI()));
      return Schema.fromString(resp.getResponse());
    } catch (HttpErrorStatusException | URISyntaxException e) {
      throw new IOException(e);
    }
  }

  public void deleteSchema(String schemaName)
      throws IOException {
    String url = _controllerRequestURLBuilder.forSchemaDelete(schemaName);
    try {
      HttpClient.wrapAndThrowHttpException(_httpClient.sendDeleteRequest(new URL(url).toURI()));
    } catch (HttpErrorStatusException | URISyntaxException e) {
      throw new IOException(e);
    }
  }

  public void addTableConfig(TableConfig tableConfig)
      throws IOException {
    try {
      HttpClient.wrapAndThrowHttpException(_httpClient.sendJsonPostRequest(new URL(
          _controllerRequestURLBuilder.forTableCreate()).toURI(), tableConfig.toJsonString()));
    } catch (HttpErrorStatusException | URISyntaxException e) {
      throw new IOException(e);
    }
  }

  public void updateTableConfig(TableConfig tableConfig)
      throws IOException {
    try {
      HttpClient.wrapAndThrowHttpException(_httpClient.sendJsonPutRequest(new URL(
          _controllerRequestURLBuilder.forUpdateTableConfig(tableConfig.getTableName())).toURI(),
          tableConfig.toJsonString()));
    } catch (HttpErrorStatusException | URISyntaxException e) {
      throw new IOException(e);
    }
  }

  public void deleteTable(String tableNameWithType)
      throws IOException {
    try {
      HttpClient.wrapAndThrowHttpException(_httpClient.sendDeleteRequest(new URL(
          _controllerRequestURLBuilder.forTableDelete(tableNameWithType)).toURI()));
    } catch (HttpErrorStatusException | URISyntaxException e) {
      throw new IOException(e);
    }
  }

  public TableConfig getTableConfig(String tableName, TableType tableType)
      throws IOException {
    try {
      SimpleHttpResponse response = HttpClient.wrapAndThrowHttpException(
          _httpClient.sendGetRequest(new URL(_controllerRequestURLBuilder.forTableGet(tableName)).toURI()));
      return JsonUtils.jsonNodeToObject(JsonUtils.stringToJsonNode(response.getResponse()).get(tableType.toString()),
          TableConfig.class);
    } catch (HttpErrorStatusException | URISyntaxException e) {
      throw new IOException(e);
    }
  }

  public long getTableSize(String tableName)
      throws IOException {
    try {
      SimpleHttpResponse response = HttpClient.wrapAndThrowHttpException(
          _httpClient.sendGetRequest(new URL(_controllerRequestURLBuilder.forTableSize(tableName)).toURI()));
      return Long.parseLong(JsonUtils.stringToJsonNode(response.getResponse()).get("reportedSizeInBytes").asText());
    } catch (HttpErrorStatusException | URISyntaxException e) {
      throw new IOException(e);
    }
  }

  public void reloadTable(String tableName, TableType tableType, boolean forceDownload)
      throws IOException {
    try {
      HttpClient.wrapAndThrowHttpException(_httpClient.sendJsonPostRequest(new URL(
          _controllerRequestURLBuilder.forTableReload(tableName, tableType, forceDownload)).toURI(), null));
    } catch (HttpErrorStatusException | URISyntaxException e) {
      throw new IOException(e);
    }
  }

  public void reloadSegment(String tableName, String segmentName, boolean forceReload)
      throws IOException {
    try {
      HttpClient.wrapAndThrowHttpException(_httpClient.sendJsonPostRequest(new URL(
          _controllerRequestURLBuilder.forSegmentReload(tableName, segmentName, forceReload)).toURI(), null));
    } catch (HttpErrorStatusException | URISyntaxException e) {
      throw new IOException(e);
    }
  }

  public void deleteSegments(String tableName, TableType tableType)
      throws IOException {
    try {
      HttpClient.wrapAndThrowHttpException(_httpClient.sendDeleteRequest(new URL(
          _controllerRequestURLBuilder.forSegmentDeleteAllAPI(tableName, tableType.toString())).toURI()));
    } catch (HttpErrorStatusException | URISyntaxException e) {
      throw new IOException(e);
    }
  }

  public void createBrokerTenant(String tenantName, int numBrokers)
      throws IOException {
    try {
      HttpClient.wrapAndThrowHttpException(_httpClient.sendJsonPostRequest(new URL(
          _controllerRequestURLBuilder.forTenantCreate()).toURI(),
          getBrokerTenantRequestPayload(tenantName, numBrokers)));
    } catch (HttpErrorStatusException | URISyntaxException e) {
      throw new IOException(e);
    }
  }

  public void updateBrokerTenant(String tenantName, int numBrokers)
      throws IOException {
    try {
      HttpClient.wrapAndThrowHttpException(_httpClient.sendJsonPutRequest(new URL(
          _controllerRequestURLBuilder.forTenantCreate()).toURI(),
          getBrokerTenantRequestPayload(tenantName, numBrokers)));
    } catch (HttpErrorStatusException | URISyntaxException e) {
      throw new IOException(e);
    }
  }

  public void createServerTenant(String tenantName, int numOfflineServers, int numRealtimeServers)
      throws IOException {
    try {
      HttpClient.wrapAndThrowHttpException(_httpClient.sendJsonPostRequest(new URL(
          _controllerRequestURLBuilder.forTenantCreate()).toURI(),
          getServerTenantRequestPayload(tenantName, numOfflineServers, numRealtimeServers)));
    } catch (HttpErrorStatusException | URISyntaxException e) {
      throw new IOException(e);
    }
  }

  public void updateServerTenant(String tenantName, int numOfflineServers, int numRealtimeServers)
      throws IOException {
    try {
      HttpClient.wrapAndThrowHttpException(_httpClient.sendJsonPutRequest(new URL(
          _controllerRequestURLBuilder.forTenantCreate()).toURI(),
          getServerTenantRequestPayload(tenantName, numOfflineServers, numRealtimeServers)));
    } catch (HttpErrorStatusException | URISyntaxException e) {
      throw new IOException(e);
    }
  }

  protected String getBrokerTenantRequestPayload(String tenantName, int numBrokers) {
    return new Tenant(TenantRole.BROKER, tenantName, numBrokers, 0, 0).toJsonString();
  }

  protected static String getServerTenantRequestPayload(String tenantName, int numOfflineServers,
      int numRealtimeServers) {
    return new Tenant(TenantRole.SERVER, tenantName, numOfflineServers + numRealtimeServers, numOfflineServers,
        numRealtimeServers).toJsonString();
  }
}
