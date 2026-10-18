package com.duesguard.model;

public interface Prioritizable {
    double calculatePriorityScore(Client client);
    double getPriorityScore();
    void setPriorityScore(double score);
}
