/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nl.dtls.fairdatapoint.service;

/**
 *
 * @author rajaram
 */
public class MyconsentServiceException extends Exception {

    /**
     * Creates a new instance of <code>MyconsentServiceException</code> without detail message.
     */
    public MyconsentServiceException() {
    }

    /**
     * Constructs an instance of <code>MyconsentServiceException</code> with the specified detail
     * message.
     *
     * @param msg the detail message.
     */
    public MyconsentServiceException(String msg) {
        super(msg);
    }
}
