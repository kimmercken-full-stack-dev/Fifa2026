export type Profiel = {
  username: string;
  voornaam: string;
  familienaam: string;
  score: number;
  admin: boolean;
  rol: 'ADMIN' | 'USER';
};
