package com.aegis.erp.modules.planilla.repository;

import com.aegis.erp.modules.planilla.entity.PlanillaCabecera;
import com.aegis.erp.modules.planilla.entity.PeriodoPlanillaId;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PlanillaCabeceraRepository extends JpaRepository<PlanillaCabecera, PeriodoPlanillaId> {}
