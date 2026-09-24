import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { environment } from '../../../../environments/environment';

export interface PeriodoCalculo {
  anio:number; mes:number; fechaInicio:string|null; fechaFin:string|null;
  fechaCreacion:string; usuarioCreacion:string; fechaModificacion:string|null;
  usuarioModificacion:string|null; planillaRegistrada:boolean;
}

@Injectable({providedIn:'root'})
export class CalculoPlanillaService {
  private readonly http=inject(HttpClient);
  private readonly url=`${environment.apiUrl}/planilla/calculo/periodos`;
  listar(){return this.http.get<PeriodoCalculo[]>(this.url);}
  obtener(anio:number,mes:number){return this.http.get<PeriodoCalculo>(`${this.url}/${anio}/${mes}`);}
}
