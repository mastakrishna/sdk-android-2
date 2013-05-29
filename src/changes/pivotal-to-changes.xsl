<?xml version="1.0"?>
<!--
Copyright 2013 Medium Entertainment, Inc.

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
-->
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:xsd="http://www.w3.org/1999/XSL/Transform">
    <xsl:output method='xml' indent='yes' encoding='UTF-8'/>
    <xsl:template match="/">
        <xsd:apply-templates />
    </xsl:template>

    <xsl:template match="stories">
        <release version="UNKNOWN">
            <xsl:for-each select="story">
                <xsl:if test="story_type='release'">
                    <xsl:attribute name="description">
                        <xsl:value-of select="name" />
                    </xsl:attribute>
                    <!--<xsl:attribute name="date">FIX_ME <xsl:value-of select="accepted_at"/></xsl:attribute>-->
                    <xsl:variable name="fulldate" select="concat(normalize-space(accepted_at), ' ')" />
                    <xsl:variable name="date" select="substring-before($fulldate, ' ')" />
                    <xsl:variable name="fixed" select="translate($date,'/','-')"/>
                    <xsl:attribute name="date"><xsl:value-of select="$fixed"/></xsl:attribute>
                </xsl:if>
            </xsl:for-each>
            <xsl:for-each select="story">
                <xsl:choose>
                    <xsl:when test="story_type='release'">
                    </xsl:when>
                    <!-- Features/Bugs that have been accepted, or Chores -->
                    <xsl:when test="current_state='accepted'">
                        <action>
                            <xsl:choose>
                                <xsl:when test="story_type='feature'">
                                    <xsl:attribute name="type">add</xsl:attribute>
                                </xsl:when>
                                <xsl:when test="story_type='chore'">
                                    <xsl:attribute name="type">update</xsl:attribute>
                                </xsl:when>
                                <xsl:when test="story_type='bug'">
                                    <xsl:attribute name="type">fix</xsl:attribute>
                                </xsl:when>
                            </xsl:choose>
                            <xsl:attribute name="dev"><xsl:value-of select="owned_by" /></xsl:attribute>
                            <xsl:value-of select="name" />
                        </action>
                    </xsl:when>
                    <!-- QA process has changed. They will not accept before this report has been run -->
                    <xsl:when test="current_state='delivered'">
                        <action>
                            <xsl:choose>
                                <xsl:when test="story_type='feature'">
                                    <xsl:attribute name="type">add</xsl:attribute>
                                </xsl:when>
                                <xsl:when test="story_type='chore'">
                                    <xsl:attribute name="type">update</xsl:attribute>
                                </xsl:when>
                                <xsl:when test="story_type='bug'">
                                    <xsl:attribute name="type">fix</xsl:attribute>
                                </xsl:when>
                            </xsl:choose>
                            <xsl:attribute name="dev"><xsl:value-of select="owned_by" /></xsl:attribute>
                            <xsl:value-of select="name" />
                        </action>
                    </xsl:when>
                    <xsl:otherwise>
                        <xsl:comment>
                            NOT DELIVERED: <xsl:value-of select="name" />
                        </xsl:comment>
                    </xsl:otherwise>
                </xsl:choose>
            </xsl:for-each>
        </release>
    </xsl:template>
</xsl:stylesheet> 
