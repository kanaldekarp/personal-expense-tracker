--
-- PostgreSQL database dump
--

-- Dumped from database version 17.2
-- Dumped by pg_dump version 17.2

-- Started on 2026-02-11 11:21:24

SET statement_timeout = 0;
SET lock_timeout = 0;
SET idle_in_transaction_session_timeout = 0;
SET transaction_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SELECT pg_catalog.set_config('search_path', '', false);
SET check_function_bodies = false;
SET xmloption = content;
SET client_min_messages = warning;
SET row_security = off;

SET default_tablespace = '';

SET default_table_access_method = heap;

--
-- TOC entry 220 (class 1259 OID 24619)
-- Name: expenses; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.expenses (
    id integer NOT NULL,
    user_id integer,
    title character varying(100),
    category character varying(50),
    amount numeric(10,2),
    date date,
    description text
);


ALTER TABLE public.expenses OWNER TO postgres;

--
-- TOC entry 219 (class 1259 OID 24618)
-- Name: expenses_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.expenses_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.expenses_id_seq OWNER TO postgres;

--
-- TOC entry 4910 (class 0 OID 0)
-- Dependencies: 219
-- Name: expenses_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.expenses_id_seq OWNED BY public.expenses.id;


--
-- TOC entry 218 (class 1259 OID 24610)
-- Name: users; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.users (
    id integer NOT NULL,
    username character varying(50) NOT NULL,
    password character varying(100) NOT NULL,
    email character varying(100) NOT NULL,
    currency character varying(10) DEFAULT 'INR'
);

CREATE TABLE public.budgets (
    id SERIAL PRIMARY KEY,
    user_id INTEGER NOT NULL REFERENCES public.users(id),
    category character varying(50) NOT NULL,
    budget_amount numeric(10,2) NOT NULL,
    month integer NOT NULL,
    year integer NOT NULL,
    UNIQUE(user_id, category, month, year)
);


ALTER TABLE public.users OWNER TO postgres;

--
-- TOC entry 217 (class 1259 OID 24609)
-- Name: users_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.users_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.users_id_seq OWNER TO postgres;

--
-- TOC entry 4911 (class 0 OID 0)
-- Dependencies: 217
-- Name: users_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.users_id_seq OWNED BY public.users.id;


--
-- TOC entry 4748 (class 2604 OID 24622)
-- Name: expenses id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.expenses ALTER COLUMN id SET DEFAULT nextval('public.expenses_id_seq'::regclass);


--
-- TOC entry 4747 (class 2604 OID 24613)
-- Name: users id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.users ALTER COLUMN id SET DEFAULT nextval('public.users_id_seq'::regclass);


--
-- TOC entry 4904 (class 0 OID 24619)
-- Dependencies: 220
-- Data for Name: expenses; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.expenses (id, user_id, title, category, amount, date, description) FROM stdin;
7	2	Milk	Grociryy	250.00	2025-04-02	NA
8	2	Test	food	76.00	2025-04-07	rewgrew
9	2	Test	food	76.00	2025-04-07	rewgrew
10	2	qr	Food	123568.00	2025-04-16	dvgdxfnhfcx
11	2	fhuijdsnhk	Food	1990.00	2025-04-09	dvbdzxf
12	2	fhuijdsnhk	Food	1990.00	2025-04-09	hjr
13	2	Test	Food	12435.00	2025-04-07	tsr
14	2	qr	Education	12435.00	2025-04-09	rhdr
15	2	Rutul 	Others	350.00	2025-04-11	Guy
16	2	eet	Travel	12.00	2025-04-16	fry
18	2	YUp	Food	1000.00	2025-04-11	qwerty
19	2	pant	Shopping	1000.00	2025-04-19	Pant 
20	2	T-shirt	Shopping	600.00	2025-04-20	
21	2	T-shirt	Travel	600.00	2025-04-19	
22	2	Test	Entertainment	1212321.00	2025-04-21	
23	2	Test	Shopping	1212321.00	2025-04-24	s
24	2	final	Travel	15000.00	2025-04-24	
25	2	qr	Travel	15000.00	2025-05-07	
26	2	aata	Food	100.00	2025-10-07	
77	1	Utilities	Utilities	453.00	2025-10-09	Electricity Bill
78	1	Food	Food	435.00	2025-09-20	Snacks
79	1	Health	Health	483.00	2026-01-19	Online Course
80	1	Shopping	Shopping	287.00	2025-10-08	Movie Ticket
81	1	Entertainment	Entertainment	150.00	2025-09-13	Uber ride
82	1	Health	Health	335.00	2025-09-22	Snacks
83	1	Utilities	Utilities	206.00	2025-12-31	Amazon Order
84	1	Entertainment	Entertainment	205.00	2025-09-20	Amazon Order
85	1	Food	Food	258.00	2025-11-14	Amazon Order
86	1	Health	Health	248.00	2025-11-12	Amazon Order
87	1	Shopping	Shopping	433.00	2025-09-03	Movie Ticket
88	1	Entertainment	Entertainment	495.00	2026-02-03	Movie Ticket
89	1	Food	Food	411.00	2025-10-27	Electricity Bill
90	1	Shopping	Shopping	336.00	2026-01-09	Amazon Order
91	1	Utilities	Utilities	498.00	2025-11-28	Doctor Visit
92	1	Entertainment	Entertainment	138.00	2025-11-19	Amazon Order
93	1	Travel	Travel	283.00	2025-08-25	Snacks
94	1	Entertainment	Entertainment	316.00	2025-09-12	Coffee
95	1	Shopping	Shopping	473.00	2025-09-15	Coffee
96	1	Travel	Travel	340.00	2025-08-30	Coffee
97	1	Shopping	Shopping	416.00	2025-10-21	Uber ride
98	1	Entertainment	Entertainment	451.00	2025-10-05	Movie Ticket
99	1	Food	Food	359.00	2025-09-25	Uber ride
100	1	Health	Health	326.00	2025-08-20	Coffee
101	1	Utilities	Utilities	109.00	2026-01-07	Electricity Bill
102	1	Utilities	Utilities	330.00	2025-10-18	Train Ticket
103	1	Utilities	Utilities	359.00	2025-12-15	Snacks
104	1	Food	Food	367.00	2025-08-17	Lunch at Subway
105	1	Travel	Travel	253.00	2026-02-05	Electricity Bill
106	1	Utilities	Utilities	328.00	2025-11-01	Doctor Visit
107	1	Shopping	Shopping	476.00	2025-12-27	Movie Ticket
108	1	Shopping	Shopping	398.00	2025-08-12	Movie Ticket
109	1	Entertainment	Entertainment	327.00	2025-10-30	Uber ride
110	1	Utilities	Utilities	498.00	2025-09-09	Snacks
111	1	Shopping	Shopping	202.00	2025-11-07	Snacks
112	1	Food	Food	406.00	2025-10-28	Uber ride
113	1	Travel	Travel	105.00	2025-08-27	Amazon Order
114	1	Health	Health	442.00	2025-10-10	Lunch at Subway
115	1	Entertainment	Entertainment	52.00	2026-01-02	Movie Ticket
116	1	Entertainment	Entertainment	97.00	2025-09-19	Coffee
117	1	Health	Health	146.00	2025-12-20	Snacks
118	1	Utilities	Utilities	371.00	2025-08-23	Lunch at Subway
119	1	Health	Health	134.00	2025-09-17	Lunch at Subway
120	1	Utilities	Utilities	234.00	2025-10-24	Amazon Order
121	1	Entertainment	Entertainment	342.00	2025-11-26	Lunch at Subway
122	1	Utilities	Utilities	408.00	2025-12-03	Amazon Order
123	1	Entertainment	Entertainment	291.00	2025-12-08	Coffee
124	1	Shopping	Shopping	331.00	2025-12-25	Amazon Order
125	1	Food	Food	321.00	2025-11-29	Electricity Bill
126	1	Food	Food	496.00	2025-10-26	Snacks
127	7	mlk;	Food	100.00	2025-10-07	
\.


--
-- TOC entry 4902 (class 0 OID 24610)
-- Dependencies: 218
-- Data for Name: users; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.users (id, username, password) FROM stdin;
2	admin	admin123
3	priyanshu	priyanshu123
4	ram	ram
6	tam 	1234
1	testuser	hashedpassword
7	vikram	priyanshu
\.


--
-- TOC entry 4912 (class 0 OID 0)
-- Dependencies: 219
-- Name: expenses_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.expenses_id_seq', 127, true);


--
-- TOC entry 4913 (class 0 OID 0)
-- Dependencies: 217
-- Name: users_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.users_id_seq', 7, true);


--
-- TOC entry 4754 (class 2606 OID 24624)
-- Name: expenses expenses_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.expenses
    ADD CONSTRAINT expenses_pkey PRIMARY KEY (id);


--
-- TOC entry 4750 (class 2606 OID 24615)
-- Name: users users_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.users
    ADD CONSTRAINT users_pkey PRIMARY KEY (id);


--
-- TOC entry 4752 (class 2606 OID 24617)
-- Name: users users_username_key; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.users
    ADD CONSTRAINT users_username_key UNIQUE (username);


--
-- TOC entry 4755 (class 2606 OID 24625)
-- Name: expenses expenses_user_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.expenses
    ADD CONSTRAINT expenses_user_id_fkey FOREIGN KEY (user_id) REFERENCES public.users(id);


-- Completed on 2026-02-11 11:21:25

--
-- PostgreSQL database dump complete
--

