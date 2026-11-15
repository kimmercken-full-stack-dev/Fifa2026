import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { provideRouter, Router } from '@angular/router';
import { RouterTestingHarness } from '@angular/router/testing';
import { routes } from '../config/app.routes';
import { App } from './app';

describe('App', () => {
  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [App],
      providers: [provideHttpClient(), provideHttpClientTesting(), provideRouter(routes)],
    })
      .compileComponents();
  });

  it('should create the app', () => {
    const fixture = TestBed.createComponent(App);
    const app = fixture.componentInstance;
    expect(app).toBeTruthy();
  });

  it('should render title', async () => {
    const fixture = TestBed.createComponent(App);
    fixture.detectChanges();
    await TestBed.inject(Router).navigateByUrl('/');
    await fixture.whenStable();
    const compiled = fixture.nativeElement as HTMLElement;
    expect(compiled.querySelector('h1')?.textContent).toContain('Wedstrijdoverzicht');
  });

  it('submits the login form as URL-encoded credentials', async () => {
    const harness = await RouterTestingHarness.create('/login');
    await harness.fixture.whenStable();
    const form = harness.routeNativeElement!.querySelector('form')!;
    const username = form.querySelector<HTMLInputElement>('#username')!;
    const password = form.querySelector<HTMLInputElement>('#password')!;
    username.value = 'tester';
    password.value = 'test+password';
    username.dispatchEvent(new Event('input'));
    password.dispatchEvent(new Event('input'));
    form.dispatchEvent(new Event('submit', { cancelable: true }));

    const http = TestBed.inject(HttpTestingController);
    http.expectOne('/api/csrf').flush({ headerName: 'X-CSRF-TOKEN', token: 'test-token' });
    const login = http.expectOne({ method: 'POST', url: '/api/login' });
    expect(login.request.serializeBody()).toBe('username=tester&password=test%2Bpassword');
    login.flush(null, { status: 401, statusText: 'Unauthorized' });
    http.verify();
  });
});
