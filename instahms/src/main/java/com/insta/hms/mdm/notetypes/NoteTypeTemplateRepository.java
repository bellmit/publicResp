package com.insta.hms.mdm.notetypes;

import com.insta.hms.mdm.MasterRepository;

import org.springframework.stereotype.Repository;

@Repository
public class NoteTypeTemplateRepository extends MasterRepository<Integer> {

  public NoteTypeTemplateRepository() {
    super("note_type_template_master", "template_id", null, 
        new String[] { "template_id","template_name" });
  }
  
}