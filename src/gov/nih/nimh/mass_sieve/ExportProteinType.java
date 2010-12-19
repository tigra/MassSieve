/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package gov.nih.nimh.mass_sieve;

/**
 *
 * @author Alex
 */
public enum ExportProteinType {

    PREFERRED,
    ALL;

    public static ExportProteinType parse(int type) {
        return (0 == type) ? PREFERRED : ALL;
    }
}
