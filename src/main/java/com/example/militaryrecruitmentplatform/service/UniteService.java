package com.example.militaryrecruitmentplatform.service;

import com.example.militaryrecruitmentplatform.exception.ResourceNotFoundException;
import com.example.militaryrecruitmentplatform.model.Unite;
import com.example.militaryrecruitmentplatform.repository.UniteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UniteService {

    private final UniteRepository uniteRepository;

    public List<Unite> getAllUnites() {
        return uniteRepository.findAll();
    }

    public Unite getUniteById(Long id) {
        return uniteRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Unité non trouvée avec l'id: " + id));
    }

    public Unite createUnite(Unite unite) {
        return uniteRepository.save(unite);
    }

    public Unite updateUnite(Long id, Unite uniteDetails) {
        Unite unite = getUniteById(id);
        unite.setNom(uniteDetails.getNom());
        unite.setGouvernorat(uniteDetails.getGouvernorat());
        unite.setType(uniteDetails.getType());
        return uniteRepository.save(unite);
    }

    public void deleteUnite(Long id) {
        Unite unite = getUniteById(id);
        uniteRepository.delete(unite);
    }
}