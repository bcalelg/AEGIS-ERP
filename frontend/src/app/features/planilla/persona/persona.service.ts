import { HttpClient, HttpParams } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { environment } from '../../../../environments/environment';

export interface Persona { id:number; nombre:string; apellido:string; fechaNacimiento:string; generoId:number; generoNombre:string; direccion:string; telefono:string; correoElectronico:string|null; estadoCivilId:number; estadoCivilNombre:string; }
export interface PersonaRequest { nombre:string; apellido:string; fechaNacimiento:string; generoId:number; direccion:string; telefono:string; correoElectronico:string|null; estadoCivilId:number; }
export interface CatalogOption { id:number; nombre:string; }
@Injectable({providedIn:'root'})
export class PersonaService {
  private readonly http=inject(HttpClient); private readonly url=`${environment.apiUrl}/planilla/personas`;
  list(search=''){let p=new HttpParams();if(search.trim())p=p.set('search',search.trim());return this.http.get<Persona[]>(this.url,{params:p});}
  create(body:PersonaRequest){return this.http.post<Persona>(this.url,body);}
  update(id:number,body:PersonaRequest){return this.http.put<Persona>(`${this.url}/${id}`,body);}
  delete(id:number){return this.http.delete<void>(`${this.url}/${id}`);}
  options(type:'generos'|'estados-civiles'){return this.http.get<CatalogOption[]>(`${this.url}/options/${type}`);}
  print(search=''){return this.http.get<Persona[]>(`${this.url}/print`,{params:search.trim()?{search:search.trim()}:{}});}
  export(format:'excel'|'pdf',search=''){return this.http.get(`${this.url}/export/${format}`,{params:search.trim()?{search:search.trim()}:{},responseType:'blob'});}
}
