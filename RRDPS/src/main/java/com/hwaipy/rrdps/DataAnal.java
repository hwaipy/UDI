/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.hwaipy.rrdps;

/**
 * @author Hwaipy 2015-3-24
 */
public class DataAnal {

  public static void main(String[] args) {
    String r = "229	51	264	55	271	43	276	37	264	37	270	34	281	37	278	41	287	56	274	31	319	47	301	25	332	43	335	42	295	46	277	38	261	47	277	55	309	36	291	31	303	35	331	35	299	42	313	35	294	56	344	40	321	56	363	27	364	56	330	42	197	39	168	31	222	35	177	40	221	26	195	23	203	31	210	24	217	34	241	24	230	39	237	30	219	22	205	24	234	36	250	17	209	33	191	23	222	31	202	28	218	44	237	20	259	50	216	35	243	26	255	24	262	36	242	35	279	24	276	23	293	45	270	28	155	42	114	29	139	37	139	35	186	30	202	19	170	30	207	27	202	33	192	23	187	26	166	37	196	36	218	22	201	34	223	26	145	22	146	26	166	26	150	15	195	22	175	25	220	32	188	25	175	34	207	23	207	25	196	30	241	38	223	27	254	35	242	29	102	23	91	19	111	19	99	20	105	18	139	16	127	25	109	15	116	11	131	20	116	18	112	24	143	26	138	22	155	25	156	25	108	18	121	22	123	17	111	19	146	29	141	14	118	24	151	16	127	22	125	20	162	25	140	16	171	19	186	22	180	37	167	29	84	27	83	16	91	19	79	16	117	20	94	9	89	21	97	16	119	20	95	15	104	23	111	21	113	16	111	17	129	21	104	15	91	22	91	13	83	19	102	17	116	29	113	17	118	19	119	17	134	17	107	24	121	17	134	19	128	23	125	20	132	26	153	13	59	8	49	9	46	12	47	7	56	6	49	9	68	14	68	8	49	12	44	8	56	13	51	7	67	15	67	7	61	9	53	15	45	13	42	9	53	9	50	12	69	13	57	7	60	17	69	13	51	13	57	11	62	15	69	12	79	15	76	12	72	18	83	10	21	7	16	5	31	4	21	6	24	8	24	5	24	4	26	5	27	2	34	9	33	6	27	6	32	6	33	10	31	9	33	12	22	4	24	4	29	8	26	4	33	9	29	6	29	6	27	2	26	7	26	5	43	4	26	3	35	10	33	11	41	8	31	9	1	1	2	1	2	0	1	0	1	0	3	0	0	0	0	0	3	0	0	0	5	0	3	1	1	0	3	0	4	1	3	0	2	2	2	1	1	1	4	0	5	0	0	3	3	1	2	0	1	1	2	1	1	1	1	1	7	2	2	0	2	0	1	1";
    String[] rs = r.split("\t");
    if (rs.length != 127 * 4) {
      System.out.println(rs.length);
      return;
    }
    int code0T = 0;
    int error0T = 0;
    int code1T = 0;
    int error1T = 0;
    for (int i = 0; i < 127; i++) {
      int code0 = Integer.parseInt(rs[i * 4]);
      int error0 = Integer.parseInt(rs[i * 4 + 1]);
      int code1 = Integer.parseInt(rs[i * 4 + 2]);
      int error1 = Integer.parseInt(rs[i * 4 + 3]);
      System.out.println((i + 1) + "\t" + (error0 / ((double) code0 + error0)) + "\t" + (error1 / ((double) code1 + error1)) + "\t" + (((error0 / ((double) code0 + error0)) + (error1 / ((double) code1 + error1))) / 2));
      code0T += code0;
      error0T += error0;
      code1T += code1;
      error1T += error1;
    }
//    System.out.println(code0T + code1T + error0T + error1T);
//    System.out.println((error0T + error1T) / ((double) code0T + code1T + error0T + error1T));
  }

}
