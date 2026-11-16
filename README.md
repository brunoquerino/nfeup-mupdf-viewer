# NFeUp MuPDF Viewer

Fork público do MuPDF Android Viewer usado pelo NFe Visualizador. O módulo
`lib` preserva as extensões necessárias para renderizar a primeira página,
extrair texto e localizar códigos de barras sem manter a árvore JNI no
aplicativo consumidor.

## Dependência

```gradle
repositories {
    maven { url 'https://brunoquerino.github.io/nfeup-mupdf-viewer/' }
}

dependencies {
    implementation 'br.com.nfeup.libs:mupdf-viewer:1.26.10a-nfeup.1'
}
```

O artefato declara `com.artifex.mupdf:fitz:1.27.1` como dependência
transitiva. Uma versão publicada é imutável; alterações exigem uma nova versão
e uma nova tag.

## Diferenças do upstream

- callback único após a renderização da primeira página;
- entrega do bitmap, texto estruturado e códigos de barras encontrados;
- extração fora da thread de interface;
- tratamento defensivo de dimensões e bitmaps inválidos;
- callback de toque prolongado;
- publicação independente como AAR Maven.

O histórico e o `README` original foram preservados para manter a procedência.

## Publicação

Tags `v*` executam `.github/workflows/publicar-maven.yml`, montam o AAR e
publicam o layout Maven na branch `gh-pages`. Não inicialize `jni` ou seus
submódulos: esta distribuição consome o `fitz` oficial pelo Maven.

## Licença e origem

O código deriva de
[`ArtifexSoftware/mupdf-android-viewer`](https://github.com/ArtifexSoftware/mupdf-android-viewer)
e permanece sob a GNU Affero General Public License v3 ou posterior, conforme
`COPYING`.
