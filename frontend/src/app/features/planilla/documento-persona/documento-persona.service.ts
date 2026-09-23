import { HttpClient, HttpParams } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { environment } from '../../../../environments/environment';
import { CatalogOption } from '../persona/persona.service';
export interface DocumentoPersona {id:string;tipoDocumentoId:number;tipoDocumentoNombre:string;personaId:number;personaNombre:string;numeroDocumento:string|null;}
export interface DocumentoPersonaRequest {tipoDocumentoId:number;personaId:number;numeroDocumento:string|null;}
@Injectable({providedIn:'root'}) export class DocumentoPersonaService {
 private readonly http=inject(HttpClient);private readonly url=`${environment.apiUrl}/planilla/documentos-persona`;
 list(search=''){let p=new HttpParams();if(search.trim())p=p.set('search',search.trim());return this.http.get<DocumentoPersona[]>(this.url,{params:p});}
 create(body:DocumentoPersonaRequest){return this.http.post<DocumentoPersona>(this.url,body);}
 update(tipoId:number,personaId:number,body:DocumentoPersonaRequest){return this.http.put<DocumentoPersona>(`${this.url}/${tipoId}/${personaId}`,body);}
 delete(tipoId:number,personaId:number){return this.http.delete<void>(`${this.url}/${tipoId}/${personaId}`);}
 personas(search=''){return this.http.get<CatalogOption[]>(`${this.url}/options/personas`,{params:search.trim()?{search:search.trim()}:{}});}
 tipos(){return this.http.get<CatalogOption[]>(`${this.url}/options/tipos-documento`);}
 print(search=''){return this.http.get<DocumentoPersona[]>(`${this.url}/print`,{params:search.trim()?{search:search.trim()}:{}});}
 export(format:'excel'|'pdf',search=''){return this.http.get(`${this.url}/export/${format}`,{params:search.trim()?{search:search.trim()}:{},responseType:'blob'});}
}
