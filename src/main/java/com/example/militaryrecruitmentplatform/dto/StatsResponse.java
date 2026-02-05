package com.example.militaryrecruitmentplatform.dto;

public class StatsResponse {

    private long total;
    private long enAttente;
    private long enExamen;
    private long validees;
    private long rejetees;

    // Constructeurs
    public StatsResponse() {}

    public StatsResponse(long total, long enAttente, long enExamen, long validees, long rejetees) {
        this.total = total;
        this.enAttente = enAttente;
        this.enExamen = enExamen;
        this.validees = validees;
        this.rejetees = rejetees;
    }

    // Getters et Setters
    public long getTotal() {
        return total;
    }

    public void setTotal(long total) {
        this.total = total;
    }

    public long getEnAttente() {
        return enAttente;
    }

    public void setEnAttente(long enAttente) {
        this.enAttente = enAttente;
    }

    public long getEnExamen() {
        return enExamen;
    }

    public void setEnExamen(long enExamen) {
        this.enExamen = enExamen;
    }

    public long getValidees() {
        return validees;
    }

    public void setValidees(long validees) {
        this.validees = validees;
    }

    public long getRejetees() {
        return rejetees;
    }

    public void setRejetees(long rejetees) {
        this.rejetees = rejetees;
    }

    // Méthodes utilitaires
    public long getTraitees() {
        return validees + rejetees;
    }

    public double getTauxValidation() {
        return total > 0 ? (double) validees / total * 100 : 0;
    }

    public double getTauxRejet() {
        return total > 0 ? (double) rejetees / total * 100 : 0;
    }

    public double getTauxTraitement() {
        return total > 0 ? (double) getTraitees() / total * 100 : 0;
    }

    @Override
    public String toString() {
        return "StatsResponse{" +
                "total=" + total +
                ", enAttente=" + enAttente +
                ", enExamen=" + enExamen +
                ", validees=" + validees +
                ", rejetees=" + rejetees +
                '}';
    }
}