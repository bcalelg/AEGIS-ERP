import { HttpClient, HttpParams } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { environment } from '../../../../environments/environment';

export interface PlanillaCatalogItem { id: string; nombre: string; relacionId: number|null; relacionNombre: string|null; statusNuevoId: number|null; statusNuevoNombre: string|null; }
export interface PlanillaCatalogRequest { nombre: string; relacionId: number|null; statusNuevoId: number|null; }

@Injectable({providedIn:'root'})
export class PlanillaCatalogService {
  private readonly http=inject(HttpClient); private readonly base=`${environment.apiUrl}/planilla/catalogos`;
  list(c:string){return this.http.get<PlanillaCatalogItem[]>(`${this.base}/${c}`);}
  options(c:string){return this.http.get<PlanillaCatalogItem[]>(`${this.base}/${c}/options`);}
  create(c:string,r:PlanillaCatalogRequest){return this.http.post<PlanillaCatalogItem>(`${this.base}/${c}`,r);}
  update(c:string,id:string,r:PlanillaCatalogRequest){return this.http.put<PlanillaCatalogItem>(`${this.base}/${c}/${id}`,r);}
  delete(c:string,id:string){return this.http.delete<void>(`${this.base}/${c}/${id}`);}
  print(c:string){return this.http.get<PlanillaCatalogItem[]>(`${this.base}/${c}/print`);}
  export(c:string,format:'excel'|'pdf',search:string){let params=new HttpParams();if(search.trim())params=params.set('search',search.trim());return this.http.get(`${this.base}/${c}/export/${format}`,{params,responseType:'blob'});}
}
