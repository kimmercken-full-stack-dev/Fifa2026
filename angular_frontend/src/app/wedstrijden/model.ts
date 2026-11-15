export type Wedstrijd = {
  wedstrijdID: number;
  land1: string;
  land2: string;
  speeldatum: string;
  starttijd: string;
  stad: string;
  stadion: string;
  uitslagLand1: number | null;
  uitslagLand2: number | null;
  winnaar: string | null;
};
