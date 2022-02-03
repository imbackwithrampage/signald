/*
 * Copyright 2022 signald contributors
 * SPDX-License-Identifier: GPL-3.0-only
 * See included LICENSE file
 *
 */

package io.finn.signald.db;

import io.finn.signald.Account;
import java.sql.SQLException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.whispersystems.libsignal.SignalProtocolAddress;
import org.whispersystems.signalservice.api.push.ACI;
import org.whispersystems.signalservice.api.push.DistributionId;

public class SenderKeySharedTable {
  private static final Logger logger = LogManager.getLogger();

  private static final String TABLE_NAME = "sender_key_shared";
  private static final String ACCOUNT_UUID = "account_uuid";
  private static final String DISTRIBUTION_ID = "distribution_id";
  private static final String DEVICE = "device";
  private static final String ADDRESS = "address";

  private final ACI aci;

  public SenderKeySharedTable(ACI aci) { this.aci = aci; }

  public Set<SignalProtocolAddress> getSenderKeySharedWith(DistributionId distributionId) {
    Set<SignalProtocolAddress> addresses = new HashSet<>();
    try {
      var query = "SELECT " + ADDRESS + "," + DEVICE + " FROM " + TABLE_NAME + " WHERE " + ACCOUNT_UUID + " = ? AND " + DISTRIBUTION_ID + " = ?";
      try (var statement = Database.getConn().prepareStatement(query)) {
        statement.setString(1, aci.toString());
        statement.setString(2, distributionId.toString());

        try (var rows = Database.executeQuery(TABLE_NAME + "_get_sender_key_shared_with", statement)) {
          while (rows.next()) {
            SignalProtocolAddress address = new SignalProtocolAddress(rows.getString(ADDRESS), rows.getInt(DEVICE));
            addresses.add(address);
          }
        }
      }
    } catch (SQLException e) {
      logger.catching(e);
    }
    return addresses;
  }

  public void markSenderKeySharedWith(DistributionId distributionId, Collection<SignalProtocolAddress> addresses) {
    try {
      var query = "INSERT OR REPLACE INTO " + TABLE_NAME + "(" + ACCOUNT_UUID + "," + ADDRESS + "," + DEVICE + "," + DISTRIBUTION_ID + ") VALUES (?, ?, ?, ?)";
      try (var statement = Database.getConn().prepareStatement(query)) {
        for (SignalProtocolAddress address : addresses) {
          statement.setString(1, aci.toString());
          statement.setString(2, address.getName());
          statement.setInt(3, address.getDeviceId());
          statement.setString(4, distributionId.toString());
          statement.addBatch();
        }
        Database.executeBatch(TABLE_NAME + "_marK_sender_key_shared_with", statement);
      }
    } catch (SQLException e) {
      logger.catching(e);
    }
  }

  public void clearSenderKeySharedWith(DistributionId distributionId, Collection<SignalProtocolAddress> addresses) {
    try {
      var query = "DELETE FROM " + TABLE_NAME + " WHERE " + ACCOUNT_UUID + " = ? AND " + ADDRESS + " = ? AND " + DEVICE + " = ? AND " + DISTRIBUTION_ID + " = ?";
      try (var statement = Database.getConn().prepareStatement(query)) {
        for (SignalProtocolAddress address : addresses) {
          statement.setString(1, aci.toString());
          statement.setString(2, distributionId.toString());
          statement.setInt(3, address.getDeviceId());
          statement.setString(4, distributionId.toString());
          statement.addBatch();
        }
        Database.executeBatch(TABLE_NAME + "_clear_sender_key_shared_with", statement);
      }
    } catch (SQLException e) {
      logger.catching(e);
    }
  }

  public void clearSenderKeySharedWith(Collection<SignalProtocolAddress> addresses) {
    try {
      var query = "DELETE FROM " + TABLE_NAME + " WHERE " + ACCOUNT_UUID + " = ? AND " + ADDRESS + " = ? AND " + DEVICE + " = ?";
      try (var statement = Database.getConn().prepareStatement(query)) {
        for (SignalProtocolAddress address : addresses) {
          statement.setString(1, aci.toString());
          statement.setInt(3, address.getDeviceId());
          statement.addBatch();
        }
        Database.executeBatch(TABLE_NAME + "_clear_sender_key_shared_with", statement);
      }
    } catch (SQLException e) {
      logger.catching(e);
    }
  }

  public boolean isMultiDevice() { return new Account(aci).getMultiDevice(); }

  public void deleteAllFor(DistributionId distributionId) throws SQLException {
    var query = "DELETE FROM " + TABLE_NAME + " WHERE " + ACCOUNT_UUID + " = ? AND " + DISTRIBUTION_ID + " = ?";
    try (var statement = Database.getConn().prepareStatement(query)) {
      statement.setString(1, aci.toString());
      statement.setString(2, distributionId.toString());
      Database.executeUpdate(TABLE_NAME + "_delete_all_for_distributionid", statement);
    }
  }

  public void deleteForAll(Recipient recipient) throws SQLException {
    var query = "DELETE FROM " + TABLE_NAME + " WHERE " + ACCOUNT_UUID + " = ? AND " + ADDRESS + " = ?";
    try (var statement = Database.getConn().prepareStatement(query)) {
      statement.setString(1, aci.toString());
      statement.setString(2, recipient.getACI().toString());
      Database.executeUpdate(TABLE_NAME + "_delete_all_for_recipient", statement);
    }
  }
  public static void deleteAccount(UUID uuid) throws SQLException {
    var query = "DELETE FROM " + TABLE_NAME + " WHERE " + ACCOUNT_UUID + " = ?";
    try (var statement = Database.getConn().prepareStatement(query)) {
      statement.setString(1, uuid.toString());
      Database.executeUpdate(TABLE_NAME + "_delete_account", statement);
    }
  }

  public void deleteSharedWith(Recipient source) throws SQLException {
    var query = "DELETE FROM " + TABLE_NAME + " WHERE " + ACCOUNT_UUID + " = ? AND " + ADDRESS + " = ?";
    try (var statement = Database.getConn().prepareStatement(query)) {
      statement.setString(1, aci.toString());
      statement.setString(2, source.getACI().toString());
      Database.executeUpdate(TABLE_NAME + "_delete_shared_with", statement);
    }
  }
}
