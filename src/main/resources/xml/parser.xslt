<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="3.0"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:cv24="http://univ.fr/cv24">

    <!-- Format des dates -->
    <xsl:template name="date-format">
        <xsl:param name="date" />
        <xsl:variable name="day" select="substring($date, 9, 2)" />
        <xsl:variable name="month" select="substring($date, 6, 2)" />
        <xsl:variable name="year" select="substring($date, 1, 4)" />
        <xsl:variable name="monthNames"
                      select="'JanFevMarAvrMaiJunJulAouSepOctNovDec'" />
        <xsl:variable name="monthIndex" select="number($month) * 3" />
        <xsl:value-of
                select="concat($day, ' ', substring($monthNames, $monthIndex - 2, 3), ' ', $year)" />
    </xsl:template>

    <!-- Format des numéros de téléphone -->
    <xsl:template name="telephone-format">
        <xsl:param name="telephone" />
        <xsl:choose>
            <xsl:when test="starts-with($telephone, '+33')">
                <xsl:value-of
                        select="concat('+33 (0)', substring($telephone, 4, 1), ' ', substring($telephone, 5, 2), ' ', substring($telephone, 7, 2), ' ', substring($telephone, 9, 2), ' ', substring($telephone, 11, 2), ' ', substring($telephone, 13, 2))" />
            </xsl:when>
            <xsl:otherwise>
                <xsl:value-of select="$telephone" />
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>

    <!--  "identite" -->
    <xsl:template match="cv24:identite">
        <h2>
            <xsl:value-of select="cv24:genre" />
            <xsl:text> </xsl:text>
            <xsl:value-of select="cv24:nom" />
            <xsl:text> </xsl:text>
            <xsl:value-of select="cv24:prenom" />
        </h2>
        <p>Tel : <xsl:call-template name="telephone-format">
            <xsl:with-param name="telephone" select="cv24:tel" />
        </xsl:call-template>
        </p>
        <p>Mel : <xsl:value-of select="cv24:mel" /></p>
    </xsl:template>

    <!--  "objectif" -->
    <xsl:template match="cv24:objectif">
        <h2>
            <xsl:value-of select="." />
        </h2>
        <p>Demande de <xsl:value-of select="@statut" /></p>
    </xsl:template>

    <!-- "prof" -->
    <xsl:template match="cv24:prof">
        <h2>Expérience professionnelle</h2>

        <!-- "detail" -->
        <ol>

            <xsl:for-each select="cv24:detail">

                <li>

                    <strong>
                        <xsl:value-of select="cv24:titre" />
                    </strong>

                    <xsl:choose>

                        <xsl:when test="cv24:datefin">

                            ( du <xsl:call-template name="date-format">

                            <xsl:with-param name="date" select="cv24:datedeb" />

                        </xsl:call-template>

                            au <xsl:call-template name="date-format">

                            <xsl:with-param name="date" select="cv24:datefin" />

                        </xsl:call-template>

                            )

                        </xsl:when>

                        <xsl:otherwise>

                            ( depuis le <xsl:call-template name="date-format">

                            <xsl:with-param name="date" select="cv24:datedeb" />

                        </xsl:call-template>

                            )

                        </xsl:otherwise>

                    </xsl:choose>

                </li>

            </xsl:for-each>

        </ol>
    </xsl:template>


    <xsl:template match="cv24:competences">
        <h2>Diplômes</h2>
        <table border="1">
            <tr>
                <th>Date</th>
                <th>Titre</th>
                <th>Niveau</th>
                <th>Institut</th>
            </tr>
            <xsl:for-each select="cv24:diplome">
                <xsl:sort select="@niveau" order="descending" />
                <tr>
                    <td>
                        <xsl:call-template name="date-format">
                            <xsl:with-param name="date" select="cv24:date" />
                        </xsl:call-template>
                    </td>
                    <td>
                        <xsl:value-of select="cv24:titreD" />
                    </td>
                    <td>
                        <xsl:value-of select="@niveau" />
                    </td>
                    <td>
                        <xsl:value-of select="cv24:institut" />
                    </td>
                </tr>
            </xsl:for-each>
        </table>

        <h2>
            <xsl:text>Certifications (</xsl:text>
            <xsl:value-of select="count(cv24:certif)" />
            <xsl:text>)</xsl:text>
        </h2>

        <table border="1">
            <tr>
                <th>Date</th>
                <th>Titre</th>
            </tr>
            <xsl:for-each select="cv24:certif">
                <tr>
                    <td>
                        <xsl:choose>
                            <xsl:when test="cv24:datefin">
                                <xsl:call-template name="date-format">
                                    <xsl:with-param name="date"
                                                    select="cv24:datedeb" />
                                </xsl:call-template>
                                <xsl:text> - </xsl:text>
                                <xsl:call-template name="date-format">
                                    <xsl:with-param name="date"
                                                    select="cv24:datefin" />
                                </xsl:call-template>

                            </xsl:when>
                            <xsl:otherwise> depuis le <xsl:call-template
                                    name="date-format">
                                <xsl:with-param name="date"
                                                select="cv24:datedeb" />
                            </xsl:call-template>

                            </xsl:otherwise>
                        </xsl:choose>

                    </td>
                    <td>
                        <xsl:value-of select="cv24:titre" />
                    </td>
                </tr>
            </xsl:for-each>
        </table>
    </xsl:template>


    <xsl:template match="cv24:divers">

        <h2>Langues</h2>

        <ul>

            <xsl:for-each select="cv24:lv">

                <li>
                    <xsl:value-of select="@lang" /><xsl:text> </xsl:text> : <xsl:value-of
                        select="@cert" /><xsl:text> </xsl:text> ( <xsl:value-of
                        select="@nivi" /> ) </li>

            </xsl:for-each>

        </ul>

        <h2>Divers</h2>

        <ul>

            <xsl:for-each select="cv24:autre">

                <li>
                    <xsl:value-of select="@titre" /><xsl:text> </xsl:text> : <xsl:value-of
                        select="@comment" /><xsl:text> </xsl:text>

                </li>

            </xsl:for-each>

        </ul>

    </xsl:template>


    <xsl:template match="/">
        <html>
            <head>
                <link rel="stylesheet" type="text/css" href="cv24.css" />
                <title>CV24 - XSLT V1.0</title>
            </head>
            <body>
                <h1>CV24 - XSLT V1.0</h1>

                <p>
                    <xsl:text>Document créé le </xsl:text>

                </p>

                <xsl:apply-templates />
            </body>
        </html>
    </xsl:template>

</xsl:stylesheet>
