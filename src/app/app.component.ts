import { Component } from '@angular/core';

@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.css']
})
export class AppComponent {
  title = 'myapp';
  constructor(){
    console.info('debug breakpoint');
    console.info('debug breakpoint remote update');
    console.log('debug breakpoint - FEATURE1 UPDATE')
    console.info('debug breakpoint - UPDATE FEATURE2');
  }
}
