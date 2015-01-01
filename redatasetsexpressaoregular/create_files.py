# {"Tipo de Câmera": [["Fotográfica Digital", "Câmera  Digital", "Fotográfica Digital e Filmadora", "Fotográfica Digital Profissional", "Câmera Fotográfica Compacta", "Fotográfica Digital e Filmadora,Fotográfica Digital e Filmadora  a prova d'água", "Câmera Digital/Gravador", "Câmera Digital de lentes intercambiáveis e flash embutido", "Fotográfica Digital Profissional c/ Filmadora", "Câmera DSLR TMT digital de lentes intercambiáveis", "Fotográfica Digital e Filmadora  a prova d'água", "Câmera Semi-Profissional", "Compacta", "Semi-profissional", "DSLR", "Câmera de ação"], 390]}

import json
import re

def replace_accents(word):
	accented_as = '[áàâãä]'
	accented_es = '[éèêẽë]'
	accented_is = '[íìîĩï]'
	accented_os = '[óòôõö]'
	accented_us = '[úùûũü]'
	word = re.sub(accented_as, 'a', word)
	word = re.sub(accented_es, 'e', word)
	word = re.sub(accented_is, 'i', word)
	word = re.sub(accented_os, 'o', word)
	word = re.sub(accented_us, 'u', word)
	word = re.sub('ç', 'c', word)
	return word

def write_attribute(key, values, fullpath):
	with open(fullpath + '/' + key + '.txt', 'w') as f:
		f.write('\n'.join(values))

def create_files(path, fpath):
	with open(path) as f:
		for l in f:
			attribute = json.loads(replace_accents(l.lower()))
			for k, v in attribute.items():
				key = re.sub('[/ ]', '_', k)
				values = v[0]
				# print(key)
				# print(values)
				write_attribute(key, values, fpath)
			# break

if __name__ == '__main__':
	# print(replace_accents('ááÄááééééííííóóóó_úúúúçççúú'.lower()))
	# create_files('AtributosCameras.json', 'cameras')
	# create_files('AtributosFilmadoras.json', 'filmadoras')
	# create_files('AtributosNotebooks.json', 'notebooks')
	# create_files('AtributosSmartphones.json', 'smartphones')
	# create_files('AtributosTvs.json', 'tvs')
