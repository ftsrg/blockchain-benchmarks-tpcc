/* SPDX-License-Identifier: Apache-2.0 */
package hu.bme.mit.ftsrg.chaincode.tpcc.api;

public final class NotFoundException extends Exception {

  NotFoundException(String message) {
    super(message);
  }
}
