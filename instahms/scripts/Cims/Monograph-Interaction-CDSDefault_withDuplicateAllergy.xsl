<?xml version="1.0"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:mims="http://www.mims.com">
	<xsl:param name="D2DSeverityFilter">^Caution^Minor^Moderate^Severe^</xsl:param>
	<xsl:param name="D2HSeverityFilter">^Extreme Caution^Contraindication^Contraindicated^</xsl:param>
	<xsl:param name="DocumentationFilter">^Not Established^Limited^Good^Well Established^</xsl:param>
	<xsl:param name="DuplicateTherapyFilter">^1^2^3^</xsl:param>
    <xsl:param name="DuplicateIngredientFilter">^1^2^3^</xsl:param>	
	<xsl:param name="CriticalPrecautionFilter">^Never use this combination.^Avoid combination.^Use combination with extreme caution.^Use combination with caution.^Clinical significance not established; be alert for evidence of interaction.^No action required; be alert for evidence of interaction.^Interaction not clinically significant.^</xsl:param>

	<xsl:output method="html" omit-xml-declaration="yes" indent="no"/>

	<!-- group allergy by prescribing item's reference and allergen reference -->
	<!--<xsl:key name="AllergyItems" match="//Allergy/*" use="concat(../../@reference, '-', @reference)"/>-->

	<xsl:key name="D2DItems" match="//Route/*[not(@Mirror='true' or @Mirror='True')]/Route/ClassInteraction" use="concat(PrescribingInteractionClass/@reference, '-', InteractingClass/@reference)"/>

	<xsl:key name="d2d_sev_level-lookup" match="mims:d2d_sev_level" use="@name"/>
	<xsl:key name="d2h_sev_level-lookup" match="mims:d2h_sev_level" use="@name"/>
	<xsl:key name="d2l_sev_level-lookup" match="mims:d2l_sev_level" use="@name"/>
	<xsl:key name="doc_level-lookup" match="mims:doc_level" use="@name"/>
	<xsl:key name="preg_cat-lookup" match="mims:preg_cat" use="@cat"/>
	<xsl:key name="preg_pic-lookup" match="mims:preg_pic" use="@cat"/>
	<xsl:variable name="d2d_sev_level-top" select="document('')/*/mims:d2d_sev_levels"/>
	<xsl:variable name="d2h_sev_level-top" select="document('')/*/mims:d2h_sev_levels"/>
	<xsl:variable name="d2l_sev_level-top" select="document('')/*/mims:d2l_sev_levels"/>
	<xsl:variable name="doc_level-top" select="document('')/*/mims:doc_levels"/>
	<xsl:variable name="preg_cat-top" select="document('')/*/mims:preg_cats"/>
	<xsl:variable name="preg_pic-top" select="document('')/*/mims:preg_pics"/>
	<xsl:variable name="criticalPrecaution">./Precaution[Professional='Use combination with extreme caution.']/Professional</xsl:variable>

	<xsl:template match="/">
		<html>
			<head>
				<link type="text/css" href="../scripts/Cims/CSS/redmond/jquery-ui-1.8.6.custom.css" rel="stylesheet" />
				<link type="text/css" href="../scripts/Cims/CSS/mims.css" rel="stylesheet" />
				<script type="text/javascript" src="../scripts/Cims/Scripts/jquery-1.4.4.min.js"/>
				<script type="text/javascript" src="../scripts/Cims/Scripts/jquery-ui-1.8.6.custom.min.js"/>
				<xsl:choose>
					<xsl:when test="count(//Interaction) &gt; 0">
						<script type="text/javascript" src="../scripts/Cims/Scripts/drugalert.js"/>
					</xsl:when>
					<xsl:otherwise>
						<script type="text/javascript" src="../scripts/Cims/Scripts/monograph.js"/>
					</xsl:otherwise>
				</xsl:choose>
				<style>
				div.drug-label {
						width: 90mm;
						height: 35mm;
				    border: 1pt solid;
				    border-radius: 5pt;
						padding: 5pt;
						margin-bottom: 5pt;
				}
				.drug-label h4 {
					font-size: 10pt;
					font-weight: bold;
					font-family: Arial;
					margin: 2pt;
				}
				.drug-label p {
					font-size: 8pt;
					font-weight: normal;
					font-family: Arial;
					margin: -1pt 2pt 2pt 2pt;
				}
				.cals-details p {
					margin-bottom: 0pt;
				}
				.cal-details li a {
				text-decoration: none;
				}

				.cal-details strong { margin-right: 2pt; }

				ol.allergy-list { padding-left: 1.33em; }
				ol.allergy-list > li { padding-top: 0.5em; }
				ol.allergy-list h4.subheading { margin-top: 0.2em; }
				</style>
			</head>
			<body>
				<xsl:apply-templates select="Result"/>
			</body>
		</html>
	</xsl:template>

	<xsl:template match="Result">
		<xsl:apply-templates/>
	</xsl:template>

	<xsl:template match="Interaction">
		<xsl:variable name="numDT" select="count(//DuplicateTherapy/Warning[contains($DuplicateTherapyFilter, concat('^',@Level, '^'))])"/>
		<xsl:variable name="numHealth" select="count(//HealthIssue/ClassInteraction[contains($D2HSeverityFilter, concat('^',Severity/@name, '^')) and contains($DocumentationFilter, concat('^',Documentation/@name, '^'))]) + count(//HealthIssueCode/ClassInteraction[contains($D2HSeverityFilter, concat('^',Severity/@name, '^')) and contains($DocumentationFilter, concat('^',Documentation/@name, '^'))])"/>
		<!-- Allergy, only count prescribing items that have at least 1 allergy. Not the total number of allergies -->
		<xsl:variable name="numAllergy" select="count(//*[Allergy])"/>
		<xsl:variable name="numInteract" select="count(//Route/*[not(@Mirror='true' or @Mirror='True')]/Route/ClassInteraction[contains($D2DSeverityFilter, concat('^',Severity/@name, '^')) and contains($DocumentationFilter, concat('^',Documentation/@name, '^'))])"/>
		<xsl:variable name="numDose" select="count(//DoseCheck/*/Warnings[count(*) &gt; 0]) + count(//CombinedDoseCheck/*/Warnings[count(*) &gt; 0])"/>
		<xsl:variable name="numPreg" select="count(//*[Pregnancy/InteractionClass]) + count(//*[WOCBA/InteractionClass])"/>
		<xsl:variable name="numLact" select="count(//Lactation/InteractionClass[count(*) &gt; 0])"/>
        <xsl:variable name="numDupIng" select="count(//DuplicateIngredient/Warning[contains($DuplicateIngredientFilter, concat('^',@Level, '^'))])"/>
		<xsl:variable name="numLabels" select="count(//*[CautionaryLabels])"/>
		<xsl:variable name="numTotal" select="$numDT + $numHealth + $numAllergy + $numInteract + $numDose + $numPreg + $numLact + $numLabels + $numDupIng"/>

        <!-- Only count number of drugs that has dose alerts -->
		<xsl:choose>

		<xsl:when test="$numTotal &gt; 0">
		<div id="tabs" class="tab-container">
			<ul>
				<xsl:if test="$numInteract &gt; 0">
					<li>
						<a href="#tab-interaction">Drug Interaction (<xsl:value-of select="$numInteract"/>)</a>
					</li>
				</xsl:if>
				<xsl:if test="$numAllergy &gt; 0">
					<li>
						<a href="#tab-allergy">Allergy (<xsl:value-of select="$numAllergy"/>)</a>
					</li>
				</xsl:if>
				<xsl:if test="$numHealth &gt; 0">
					<li>
						<a href="#tab-health">Health Conditions (<xsl:value-of select="$numHealth"/>)</a>
					</li>
				</xsl:if>
				<xsl:if test="$numDT &gt; 0">
					<li>
						<a href="#tab-dup">Duplicate Therapy (<xsl:value-of select="$numDT"/>)</a>
					</li>
				</xsl:if>
                <xsl:if test="$numDupIng &gt; 0">
                    <li>
                        <a href="#tab-duping">Duplicate Ingredient (<xsl:value-of select="$numDupIng"/>)</a>
                    </li>
                </xsl:if>
				<xsl:if test="$numDose &gt; 0">
					<li>
						<a href="#tab-dose">Dose (<xsl:value-of select="$numDose"/>)</a>
					</li>
				</xsl:if>
				<xsl:if test="$numPreg &gt; 0">
					<li>
						<a href="#tab-preg">Pregnancy (<xsl:value-of select="$numPreg"/>)</a>
					</li>
				</xsl:if>
				<xsl:if test="$numLact &gt; 0">
					<li>
						<a href="#tab-lact">Lactation (<xsl:value-of select="$numLact"/>)</a>
					</li>
				</xsl:if>
<xsl:if test="$numLabels &gt; 0">
	<li>
		<a href="#tab-labels">Cautionary Labels (<xsl:value-of select="$numLabels"/>)</a>
	</li>
</xsl:if>
			</ul>

			<xsl:if test="$numDT &gt; 0">
				<div id="tab-dup" class="acc_container">
					<div id="list_dup" style="height: 100%">
						<xsl:apply-templates select="DuplicateTherapy"/>
					</div>
				</div>
			</xsl:if>

            <xsl:if test="$numDupIng &gt; 0">
                <div id="tab-duping" class="acc_container">
                    <div id="list_duping" style="height: 100%">
                         <xsl:apply-templates select="DuplicateIngredient"/>
                    </div>
                </div>
            </xsl:if>
					
			<xsl:if test="$numInteract &gt; 0">
				<div id="tab-interaction" class="acc_container">
					<div id="list_interactions">
						<xsl:apply-templates select="(DrugItem|Product|GenericItem|SpecificItem|GGPI|ActiveComposition|ActiveCompositionGroup)">
							<xsl:with-param name="Mode" select="'drug'"/>
						</xsl:apply-templates>
					</div>
				</div>
			</xsl:if>

			<xsl:if test="$numHealth &gt; 0">
				<div id="tab-health" class="acc_container">
					<div id="list_health">
						<xsl:apply-templates select="(DrugItem|Product|GenericItem|SpecificItem|GGPI|ActiveComposition|ActiveCompositionGroup)">
							<xsl:with-param name="Mode" select="'health'"/>
						</xsl:apply-templates>
					</div>
				</div>
			</xsl:if>

			<xsl:if test="$numAllergy &gt; 0">
				<div id="tab-allergy" class="acc_container">
					<div id="list_allergies">
						<xsl:apply-templates select="(DrugItem|Product|GenericItem|SpecificItem|GGPI|ActiveComposition|ActiveCompositionGroup)">
							<xsl:with-param name="Mode" select="'allergy'"/>
						</xsl:apply-templates>
					</div>
				</div>
			</xsl:if>

			<xsl:if test="$numDose &gt; 0">
				<div id="tab-dose" class="acc_container">
					<div id="list_dose">
						<xsl:apply-templates select="DoseCheck|CombinedDoseCheck"></xsl:apply-templates>
					</div>
				</div>
			</xsl:if>

			<xsl:if test="$numPreg &gt; 0">
				<div id="tab-preg" class="acc_container">
					<div id="list_preg">
						<xsl:apply-templates select="(DrugItem|Product|GenericItem|SpecificItem|GGPI|ActiveComposition|ActiveCompositionGroup)">
							<xsl:with-param name="Mode" select="'pregnancy'"/>
						</xsl:apply-templates>
					</div>
				</div>
			</xsl:if>

			<xsl:if test="$numLact &gt; 0">
				<div id="tab-lact" class="acc_container">
					<div id="list_lact">
						<xsl:apply-templates select="(DrugItem|Product|GenericItem|SpecificItem|GGPI|ActiveComposition|ActiveCompositionGroup)">
							<xsl:with-param name="Mode" select="'lactation'"/>
						</xsl:apply-templates>
					</div>
				</div>
			</xsl:if>

<xsl:if test="$numLabels &gt; 0">
	<div id="tab-labels" class="acc_container">
		<div id="list_labels">
			<xsl:apply-templates select="(DrugItem|Product|GenericItem|SpecificItem|GGPI|ActiveComposition|ActiveCompositionGroup)[CautionaryLabels]" mode="label"/>
		</div>
	</div>
</xsl:if>

		</div>
		</xsl:when>
		<xsl:otherwise>
			<h3 class="ui-state-default ui-widget-content ui-widget-header" style="height:30px;text-align:center;padding-top: 10px">No results found. Absence of interaction result should in no way be interpreted as safe. Clinical judgment should be exercised</h3>
		</xsl:otherwise>
		</xsl:choose>

</xsl:template>

	<xsl:template match="DrugItem|Product|GenericItem|SpecificItem|GGPI|ActiveComposition|ActiveCompositionGroup">
		<xsl:param name="Mode"/>
		<xsl:variable name="IntType1" select="name()"/>
		<xsl:variable name="IntProd1" select="@name"/>
		<xsl:variable name="IntRef1" select="@reference"/>
		<xsl:choose>
			<xsl:when test="$Mode='allergy' and Allergy">
					<xsl:element name="div">
						<xsl:variable name="DivId">a_<xsl:value-of select="translate(@name, 'ABCDEFGHIJKLMNOPQRSTUVWXYZ ','abcdefghijklmnopqrstuvwxyz_')"/></xsl:variable>
						<xsl:attribute name="id"><xsl:value-of select="$DivId"/></xsl:attribute>
						<h3>
							<a href="#">Patient may be allergic to the prescribing item, <xsl:value-of select="$IntProd1"/>.</a>
						</h3>
						<div>
							<!--<xsl:for-each select="Allergy/*[count(. | key('AllergyItems', concat($IntRef1, '-', @reference))[1])=1]">-->
							<xsl:apply-templates select="Allergy">
								<xsl:with-param name="IntProd1" select="$IntProd1"/>
							</xsl:apply-templates>
						<!--</xsl:for-each>-->
						</div>
					</xsl:element>

			</xsl:when>
			<xsl:otherwise>
				<xsl:for-each select="Route">
					<xsl:variable name="IntRoute1" select="@name"/>
					<xsl:if test="$Mode='pregnancy' and Pregnancy|WOCBA">
						<xsl:variable name="DivId">pc_<xsl:value-of select="translate($IntProd1, 'ABCDEFGHIJKLMNOPQRSTUVWXYZ ','abcdefghijklmnopqrstuvwxyz_')"/></xsl:variable>
							<xsl:for-each select="WOCBA|Pregnancy">
								<!--<xsl:sort select="InteractionClass/Molecule/@name" order="ascending"/>-->
								<!-- Take the most severe item by FDA preg cat -->
								<xsl:sort select="concat(Category[@Source='FDA']/@name,'-',InteractionClass/Molecule/@name)" order="descending"/>
								<xsl:if test="position()=1"> <!-- Only loop once -->
									<xsl:element name="div">
										<xsl:attribute name="id"><xsl:value-of select="$DivId"/></xsl:attribute>
										<h3>
											<a href="#">
												<xsl:value-of select="$IntProd1"/>
												<xsl:text> (</xsl:text><xsl:value-of select="InteractionClass/Molecule/@name"/>
												<xsl:text>/</xsl:text><xsl:value-of select="../@name"/>
												<xsl:text>) [</xsl:text>
												<xsl:choose>
												<xsl:when test="name()='WOCBA'">
												<xsl:text>Women of Childbearing Age</xsl:text>
												</xsl:when>
												<xsl:when test="name()='Pregnancy'">
												<xsl:value-of select="../Pregnancy[position()=1]/Category/@Trimester"/>
												<xsl:text> Trimester</xsl:text>
												</xsl:when>
												</xsl:choose>
												<xsl:text>]</xsl:text>
											</a>
										</h3>
										<div>
											<xsl:apply-templates select="."/>
										</div>
									</xsl:element>
								</xsl:if>
							</xsl:for-each>

					</xsl:if>
					<xsl:if test="$Mode='lactation'">
						<xsl:for-each select="Lactation">
							<xsl:apply-templates select=".">
								<xsl:with-param name="IntProd" select="$IntProd1"/>
								<xsl:with-param name="Route" select="../@name"/>
							</xsl:apply-templates>
						</xsl:for-each>
					</xsl:if>

					<xsl:if test="$Mode='drug'">
						<xsl:for-each select="(DrugItem|Product|GenericItem|SpecificItem|GGPI|ActiveComposition|ActiveCompositionGroup)[not(@Mirror='true' or @Mirror='True')]">
							<xsl:variable name="IntType2" select="name()"/>
							<xsl:apply-templates select="Route">
								<xsl:with-param name="IntProd1" select="$IntProd1"/>
								<xsl:with-param name="IntProd2" select="@name"/>
								<xsl:with-param name="IntRoute1" select="$IntRoute1"/>
							</xsl:apply-templates>
						</xsl:for-each>
					</xsl:if>
					<xsl:if test="$Mode='health'">
						<xsl:apply-templates select="HealthIssueCode">
							<xsl:with-param name="IntProd1" select="$IntProd1"/>
							<xsl:with-param name="IntRoute1" select="$IntRoute1"/>
						</xsl:apply-templates>
						<xsl:apply-templates select="HealthIssue">
							<xsl:with-param name="IntProd1" select="$IntProd1"/>
							<xsl:with-param name="IntRoute1" select="$IntRoute1"/>
						</xsl:apply-templates>
					</xsl:if>
				</xsl:for-each>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>

	<xsl:template match="Route">
		<xsl:param name="IntProd1"/>
		<xsl:param name="IntProd2"/>
		<xsl:param name="IntRoute1"/>
		<xsl:param name="Mode"/>
		<xsl:variable name="IntRoute2" select="@name"/>
		<xsl:for-each select="ClassInteraction[contains($D2DSeverityFilter, concat('^',Severity/@name, '^')) and contains($DocumentationFilter, concat('^',Documentation/@name, '^'))]">
			<xsl:variable name="IntMol1" select="PrescribingInteractionClass/PrescribingMolecule/@name"/>
			<xsl:variable name="IntMol2" select="InteractionClass/Molecule/@name"/>
			<xsl:variable name="DivId">i_<xsl:value-of select="Severity/@ranking"/>_<xsl:value-of select="translate($IntMol1, 'ABCDEFGHIJKLMNOPQRSTUVWXYZ .','abcdefghijklmnopqrstuvwxyz__')"/>_<xsl:value-of select="translate($IntMol2, 'ABCDEFGHIJKLMNOPQRSTUVWXYZ .','abcdefghijklmnopqrstuvwxyz__')"/>
			</xsl:variable>
			<xsl:element name="div">
				<xsl:attribute name="id"><xsl:value-of select="$DivId"/></xsl:attribute>
				<h3>
					<a href="#">
						<xsl:call-template name="SeverityColor">
							<xsl:with-param name="SeverityLevel" select="Severity/@ranking"/>
						</xsl:call-template>
						<!-- Leo - Show critical precaution -->
						<xsl:value-of select="$IntProd1"/> (<xsl:value-of select="$IntMol1"/>/<xsl:value-of select="$IntRoute1"/>)<xsl:text> vs </xsl:text>
						<xsl:value-of select="$IntProd2"/> (<xsl:value-of select="$IntMol2"/>/<xsl:value-of select="$IntRoute2"/>)
						<xsl:call-template name="ShowCriticalPrecaution">
							<xsl:with-param name="PrecautionText" select="./Precaution[contains($CriticalPrecautionFilter,concat('^',Professional/text(),'^'))]/Professional"/>
						</xsl:call-template>
                    </a>
				</h3>
				<div>
					<h4 class="subheading">Interaction Effect</h4>
					<p><xsl:value-of select="$IntMol1"/>
					<xsl:text> </xsl:text>
					<xsl:choose>
						<xsl:when test="normalize-space(Observation/Abbreviated)">
							<xsl:value-of select="Observation/Abbreviated"/>
						</xsl:when>
						<xsl:when test="normalize-space(Observation/Professional)">
							<xsl:value-of select="Observation/Professional"/>
						</xsl:when>
					</xsl:choose>
					<xsl:text> </xsl:text>
					<xsl:value-of select="$IntMol2"/></p>

					<xsl:call-template name="d2dSeverity">
						<xsl:with-param name="Severity" select="Severity/@name"/>
					</xsl:call-template>

					<xsl:call-template name="docLevel">
						<xsl:with-param name="DocLevel" select="Documentation/@name"/>
					</xsl:call-template>

					<!-- Show the rest of content in hidden div -->
					<xsl:element name="button">
						<xsl:attribute name="name">_ref</xsl:attribute>
						<xsl:attribute name="id">_br_<xsl:value-of select="$DivId"/></xsl:attribute>
						<xsl:attribute name="class">read-more</xsl:attribute>
						<xsl:text>Read more </xsl:text>
					</xsl:element>
					<xsl:element name="div">
						<xsl:attribute name="id">r_<xsl:value-of select="$DivId"/></xsl:attribute>
						<xsl:attribute name="style">display: none</xsl:attribute>

						<h4 class="subheading">Probable Mechanism</h4>
						<xsl:choose>
							<xsl:when test="normalize-space(Interaction/Abbreviated)">
								<xsl:value-of select="Interaction/Abbreviated"/>
							</xsl:when>
							<xsl:when test="normalize-space(Interaction/Professional)">
								<xsl:value-of select="Interaction/Professional"/>
							</xsl:when>
						</xsl:choose>
						<h4 class="subheading">Actions to be taken</h4>
						<ol>
							<xsl:apply-templates select="Precaution"/>
						</ol>
						<xsl:apply-templates select="References">
							<xsl:with-param name="DivId" select="$DivId"/>
						</xsl:apply-templates>
					</xsl:element>

				</div>
			</xsl:element>
		</xsl:for-each>
	</xsl:template>

	<xsl:template name="ShowCriticalPrecaution">
		<xsl:param name="PrecautionText"/>
		<xsl:if test="normalize-space($PrecautionText)"><xsl:text> [</xsl:text><xsl:value-of select="$PrecautionText"/><xsl:text>]</xsl:text></xsl:if>
	</xsl:template>

	<xsl:template match="WOCBA|Pregnancy">
		<xsl:variable name="PCAT" select="Category[@Source='FDA']/@name"/>
		<xsl:if test="normalize-space($PCAT)">
			<h4 class="subheading">FDA Category<xsl:text> </xsl:text>
			<xsl:element name="img">
				<xsl:attribute name="src"><xsl:apply-templates select="$preg_pic-top"><xsl:with-param name="Source" select="'FDA'"/><xsl:with-param name="Cat" select="$PCAT"/></xsl:apply-templates></xsl:attribute>
			</xsl:element>
			</h4>
		<p><xsl:apply-templates select="$preg_cat-top"><xsl:with-param name="Source" select="'FDA'"/><xsl:with-param name="Cat" select="$PCAT"/></xsl:apply-templates></p>
		</xsl:if>
		<xsl:if test="count(Category[@Source='MIMS']) &gt; 0">
			<h4 class="subheading">MIMS Notes</h4>
			<p><xsl:value-of select="Category[@Source='MIMS']/Comment"/></p>
		</xsl:if>
	</xsl:template>

	<xsl:template match="Lactation">
		<xsl:param name="IntProd"/>
		<xsl:param name="Route"/>
		<xsl:variable name="DivId">la_<xsl:value-of select="translate($IntProd, 'ABCDEFGHIJKLMNOPQRSTUVWXYZ ','abcdefghijklmnopqrstuvwxyz_')"/></xsl:variable>
		<xsl:element name="div">
			<xsl:attribute name="id"><xsl:value-of select="$DivId"/></xsl:attribute>
			<h3>
				<a href="#">
					<xsl:value-of select="$IntProd"/>
				</a>
			</h3>
			<div>
				<xsl:call-template name="d2lSeverity">
					<xsl:with-param name="Severity" select="Severity/@name"/>
				</xsl:call-template>
				<xsl:value-of select="Comment"/>
			</div>
		</xsl:element>
	</xsl:template>

	<xsl:template match="HealthIssueCode">
		<xsl:param name="IntProd1"/>
		<xsl:param name="IntRoute1"/>
		<xsl:variable name="IntHealthCode" select="@code"/>
		<xsl:variable name="IntHealthCodeType" select="@codeType"/>
		<xsl:variable name="IntHealthCodeDesc" select="@name"/>
		<xsl:for-each select="ClassInteraction[contains($D2HSeverityFilter, concat('^',Severity/@name, '^')) and contains($DocumentationFilter, concat('^',Documentation/@name, '^'))]">
			<xsl:variable name="IntMol1" select="PrescribingInteractionClass/PrescribingMolecule/@name"/>
			<xsl:variable name="DivId">i_<xsl:value-of select="Severity/@ranking"/>_<xsl:value-of select="translate($IntMol1, 'ABCDEFGHIJKLMNOPQRSTUVWXYZ .','abcdefghijklmnopqrstuvwxyz__')"/>_<xsl:value-of select="translate($IntHealthCode, 'ABCDEFGHIJKLMNOPQRSTUVWXYZ .','abcdefghijklmnopqrstuvwxyz__')"/></xsl:variable>
			<xsl:element name="div">
				<xsl:attribute name="id"><xsl:value-of select="$DivId"/></xsl:attribute>
				<h3>
					<a href="#">
						<xsl:call-template name="SeverityColor">
							<!-- Health has less severity levels, so we need to map it to our 5 colors -->
							<xsl:with-param name="SeverityLevel" select="translate(Severity/@ranking, '123', '135')"/>
						</xsl:call-template>
						<xsl:value-of select="$IntProd1"/> (<xsl:value-of select="$IntMol1"/>)
                        <xsl:text> vs </xsl:text>
						<xsl:value-of select="$IntHealthCode"/> (<xsl:value-of select="$IntHealthCodeDesc"/>)
                    </a>
				</h3>
				<div>

					<xsl:call-template name="d2hSeverity">
						<xsl:with-param name="Severity" select="Severity/@name"/>
					</xsl:call-template>

					<xsl:call-template name="docLevel">
						<xsl:with-param name="DocLevel" select="Documentation/@name"/>
					</xsl:call-template>

					<!-- Show the rest of content in hidden div -->
					<xsl:element name="button">
						<xsl:attribute name="name">_ref</xsl:attribute>
						<xsl:attribute name="id">_br_<xsl:value-of select="$DivId"/></xsl:attribute>
						<xsl:attribute name="class">read-more</xsl:attribute>
						<xsl:text>Read more</xsl:text>
					</xsl:element>
					<xsl:element name="div">
						<xsl:attribute name="id">r_<xsl:value-of select="$DivId"/></xsl:attribute>
						<xsl:attribute name="style">display: none</xsl:attribute>

					<h4 class="subheading">Probable Mechanism</h4>
					<xsl:choose>
						<xsl:when test="normalize-space(Interaction/Abbreviated)">
							<xsl:value-of select="Interaction/Abbreviated"/>
						</xsl:when>
						<xsl:when test="normalize-space(Interaction/Professional)">
							<xsl:value-of select="Interaction/Professional"/>
						</xsl:when>
					</xsl:choose>

					<xsl:apply-templates select="References">
						<xsl:with-param name="DivId" select="$DivId"/>
					</xsl:apply-templates>
					</xsl:element>
				</div>
			</xsl:element>
		</xsl:for-each>
	</xsl:template>

	<xsl:template match="HealthIssue">
		<xsl:param name="IntProd1"/>
		<xsl:param name="IntRoute1"/>
		<xsl:variable name="IntHealthIssueName" select="@name"/>
		<xsl:variable name="IntHealthCodeType" select="@codeType"/>
		<xsl:for-each select="ClassInteraction[contains($D2HSeverityFilter, concat('^',Severity/@name, '^')) and contains($DocumentationFilter, concat('^',Documentation/@name, '^'))]">
			<xsl:variable name="IntMol1" select="PrescribingInteractionClass/PrescribingMolecule/@name"/>
			<xsl:variable name="DivId">i_<xsl:value-of select="Severity/@ranking"/>_<xsl:value-of select="translate($IntMol1, 'ABCDEFGHIJKLMNOPQRSTUVWXYZ .','abcdefghijklmnopqrstuvwxyz__')"/>_<xsl:value-of select="translate($IntHealthIssueName, 'ABCDEFGHIJKLMNOPQRSTUVWXYZ .','abcdefghijklmnopqrstuvwxyz__')"/></xsl:variable>
			<xsl:element name="div">
				<xsl:attribute name="id"><xsl:value-of select="$DivId"/></xsl:attribute>
				<h3>
					<a href="#">
						<xsl:call-template name="SeverityColor">
							<xsl:with-param name="SeverityLevel" select="translate(Severity/@ranking, '123', '135')"/>
						</xsl:call-template>
						<xsl:value-of select="$IntProd1"/> (<xsl:value-of select="$IntMol1"/>)
                        <xsl:text> vs </xsl:text>
						<xsl:value-of select="$IntHealthIssueName"/>
					</a>
				</h3>
				<div>

					<xsl:call-template name="d2hSeverity">
						<xsl:with-param name="Severity" select="Severity/@name"/>
					</xsl:call-template>
					<xsl:call-template name="docLevel">
						<xsl:with-param name="DocLevel" select="Documentation/@name"/>
					</xsl:call-template>
					<!-- Show the rest of content in hidden div -->
					<xsl:element name="button">
						<xsl:attribute name="name">_ref</xsl:attribute>
						<xsl:attribute name="id">_br_<xsl:value-of select="$DivId"/></xsl:attribute>
						<xsl:attribute name="class">read-more</xsl:attribute>
						<xsl:text>Read more</xsl:text>
					</xsl:element>
					<xsl:element name="div">
						<xsl:attribute name="id">r_<xsl:value-of select="$DivId"/></xsl:attribute>
						<xsl:attribute name="style">display: none</xsl:attribute>

					<h4 class="subheading">Probable Mechanism</h4>
					<xsl:choose>
						<xsl:when test="normalize-space(Interaction/Abbreviated)">
							<xsl:value-of select="Interaction/Abbreviated"/>
						</xsl:when>
						<xsl:when test="normalize-space(Interaction/Professional)">
							<xsl:value-of select="Interaction/Professional"/>
						</xsl:when>
					</xsl:choose>
					<xsl:apply-templates select="References">
						<xsl:with-param name="DivId" select="$DivId"/>
					</xsl:apply-templates>
					</xsl:element>
				</div>
			</xsl:element>
		</xsl:for-each>
	</xsl:template>

	<!--<xsl:template match="Allergy">
		<xsl:param name="IntProd1"/>
		<div>
			<h4 class="subheading">Allergy history:</h4>
			<ol class="allergy-list">
				<xsl:for-each select="*">
					<xsl:choose>
						<xsl:when test="name()='CrossSensitive'">
								<xsl:apply-templates select=".">
									<xsl:with-param name="IntProd1" select="$IntProd1"/>
								</xsl:apply-templates>
						</xsl:when>
					<xsl:when test="CrossSensitive">
							<xsl:apply-templates select="CrossSensitive">
								<xsl:with-param name="IntProd1" select="$IntProd1"/>
							</xsl:apply-templates>
					</xsl:when>
					<xsl:otherwise>
						<li>The patient has a known history of allergic reaction to <xsl:value-of select="name()"/>, <xsl:value-of select="@name"/>.
						<ul class="no-bullet-no-indent">
						<xsl:choose>
						<xsl:when test="name()='Molecule' and .//PrescribingMolecule/@name!=@name">
							<li><xsl:value-of select="$IntProd1"/> contains <xsl:value-of select=".//PrescribingMolecule/@name"/>, which belongs to the same substance class as <xsl:value-of select="@name"/>.</li>
						</xsl:when>
						<xsl:when test="name()='Molecule' and .//PrescribingMolecule/@name=@name">
							<li><xsl:value-of select="$IntProd1"/> contains <xsl:value-of select=".//PrescribingMolecule/@name"/>.</li>
						</xsl:when>
						<xsl:when test="name()='SubstanceClass'">
							<li><xsl:value-of select="$IntProd1"/> contains <xsl:value-of select=".//PrescribingMolecule/@name"/>, which belongs to that same substance class.</li>
						</xsl:when>
						</xsl:choose>
					</ul>
				</li>
					</xsl:otherwise>
					</xsl:choose>
				</xsl:for-each>
			</ol>
		</div>
	</xsl:template>

<xsl:template match="CrossSensitive">
	<xsl:param name="IntProd1"/>
	<xsl:variable name="DivId">c_<xsl:value-of select="translate($IntProd1, 'ABCDEFGHIJKLMNOPQRSTUVWXYZ ','abcdefghijklmnopqrstuvwxyz_')"/></xsl:variable>
	<xsl:variable name="AllergenType"><xsl:choose><xsl:when test="name(..) = 'Allergy'">SubstanceClass</xsl:when><xsl:otherwise><xsl:value-of select="name(..)"/></xsl:otherwise></xsl:choose></xsl:variable>
	<xsl:variable name="AllergenName"><xsl:choose><xsl:when test="name(..) = 'Allergy'"><xsl:value-of select=".//AllergySubstanceClass/@name"/></xsl:when><xsl:otherwise><xsl:value-of select="../@name"/></xsl:otherwise></xsl:choose></xsl:variable>
	<li>The patient has a known history of allergic reaction to <xsl:value-of select="$AllergenType"/>, <em><xsl:value-of select="$AllergenName"/></em>.
	<ul class="no-bullet-no-indent">
	<li><em><xsl:value-of select="$IntProd1"/></em> contains <em><xsl:value-of select=".//PrescribingMolecule/@name"/></em>, which is cross-sensitive to <em><xsl:value-of select="$AllergenName"/></em>.</li>
	</ul>
	<xsl:apply-templates select="References">
		<xsl:with-param name="DivId" select="$DivId"/>
	</xsl:apply-templates>
</li>
</xsl:template> -->
<xsl:template match="Allergy">
		<xsl:param name="IntProd1"/>
		
		<div>
		
			<h4 class="subheading">Allergy history:</h4>
			<ol class="allergy-list">
				<xsl:for-each select="*">
				
					<xsl:choose>
						
							<xsl:when test="name()='CrossSensitive'">
							
								 <xsl:variable name="DivId">c_<xsl:value-of select="translate($IntProd1, 'ABCDEFGHIJKLMNOPQRSTUVWXYZ ','abcdefghijklmnopqrstuvwxyz_')"/></xsl:variable>
									<li>The patient has a known history of allergic reaction to SubstanceClass, <em><xsl:value-of select="AllergySubstanceClass/@name|SubstanceClass/@name"/></em>.<ul class="no-bullet-no-indent">
									 
								<li><em><xsl:value-of select="$IntProd1"/></em> contains <em><xsl:value-of select=".//PrescribingMolecule/@name"/></em>, which is cross-sensitive to
									<em><xsl:value-of select="AllergySubstanceClass/@name|SubstanceClass/@name"/></em>.</li>
									<xsl:apply-templates select="References">
										<xsl:with-param name="DivId" select="$DivId"/>
									</xsl:apply-templates>
								  </ul></li>
							</xsl:when>
							<xsl:when test="CrossSensitive">
								
								<xsl:variable name="DivId">c_<xsl:value-of select="translate($IntProd1, 'ABCDEFGHIJKLMNOPQRSTUVWXYZ ','abcdefghijklmnopqrstuvwxyz_')"/></xsl:variable>
								<li>The patient has a known history of allergic reaction to <xsl:value-of select="name()"/>, <em><xsl:value-of select="@name"/></em>.<ul class="no-bullet-no-indent">
								
								<li><em><xsl:value-of select="$IntProd1"/></em> contains <em><xsl:value-of select=".//PrescribingMolecule/@name"/></em>, which is cross-sensitive to <em><xsl:value-of select="@name"/></em>.</li>
								
								<xsl:apply-templates select="CrossSensitive/References">
									<xsl:with-param name="DivId" select="$DivId"/>
								</xsl:apply-templates>
								</ul></li>
							</xsl:when>
							<xsl:otherwise>
								 
								<li>The patient has a known history of allergic reaction to <xsl:value-of select="name()"/>, <em><xsl:value-of select="@name"/></em>.<ul class="no-bullet-no-indent">
								
								<xsl:choose>
									<xsl:when test="name()='Molecule' and .//PrescribingMolecule/@name!=@name">
										<li><em><xsl:value-of select="$IntProd1"/></em> contains <em><xsl:value-of select=".//PrescribingMolecule/@name"/></em>, which belongs to the same substance class as <em><xsl:value-of select="@name"/></em>. </li>
									</xsl:when>
									<xsl:when test="name()='Molecule' and .//PrescribingMolecule/@name=@name">
										<li><em><xsl:value-of select="$IntProd1"/></em> contains <em><xsl:value-of select=".//PrescribingMolecule/@name"/></em>. </li>
									</xsl:when>
									<xsl:when test="name()='SubstanceClass'">
										<li><em><xsl:value-of select="$IntProd1"/></em> contains <em><xsl:value-of select=".//PrescribingMolecule/@name"/></em>, which belongs to that same substance class.<em><xsl:value-of select="@name"/></em>.</li>
										
									</xsl:when>
									
								</xsl:choose>
							 </ul></li>
							</xsl:otherwise>
						
					</xsl:choose>
				 
				</xsl:for-each>
			 </ol>
		</div>
	</xsl:template>


	<xsl:template match="DoseCheck|CombinedDoseCheck">
		<xsl:for-each select="*[count(Warnings) &gt; 0]">
			<xsl:variable name="IntProd" select="@name"/>
			<xsl:variable name="DivId">dc_<xsl:value-of select="translate($IntProd, 'ABCDEFGHIJKLMNOPQRSTUVWXYZ ','abcdefghijklmnopqrstuvwxyz_')"/></xsl:variable>
			<xsl:element name="div">
				<xsl:attribute name="id"><xsl:value-of select="$DivId"/></xsl:attribute>
				<h3>
					<a href="#">
											<xsl:if test="name(..)='CombinedDoseCheck'">
							<xsl:text>[Combined Dose Check] </xsl:text>
						</xsl:if>
						<xsl:value-of select="$IntProd"/>
					</a>
				</h3>
				<div>
					<xsl:for-each select="Warnings/Warning">
						<div class="ui-widget">
							<div class="ui-state-default ui-corner-all warning-bar">
								<p>
									<span class="ui-icon ui-icon-alert"/>
									<xsl:choose>
										<xsl:when test="normalize-space(text())">
											<xsl:value-of select="text()"/>
										</xsl:when>
										<xsl:otherwise>
											<xsl:value-of select="Message"/>
										</xsl:otherwise>

									</xsl:choose>
									<xsl:if test="ValidRoutes|ValidForms|ValidIndications">
										<ul>
											<xsl:for-each select="*/*">
												<li>
													<xsl:choose>
														<xsl:when test="@name">
															<xsl:value-of select="@name"/>
														</xsl:when>
														<xsl:otherwise>
															<xsl:value-of select="text()"/>
														</xsl:otherwise>
													</xsl:choose>
												</li>
											</xsl:for-each>
										</ul>
									</xsl:if>
								</p>
							</div>
						</div>
					</xsl:for-each>
				</div>
			</xsl:element>
		</xsl:for-each>
		<xsl:apply-templates select="CombinedDoseCheck"/>
	</xsl:template>

	<xsl:template match="Precaution">
		<li>
			<xsl:choose>
				<xsl:when test="normalize-space(./Abbreviated)">
					<xsl:value-of select="./Abbreviated"/>
				</xsl:when>
				<xsl:when test="normalize-space(./Professional)">
					<xsl:value-of select="./Professional"/>
				</xsl:when>
			</xsl:choose>
		</li>
	</xsl:template>

	<xsl:template match="DuplicateTherapy">
		<xsl:for-each select="./Warning[contains($DuplicateTherapyFilter, concat('^',@Level, '^'))]">
			<xsl:variable name="DuplicateLevel" select="translate(@Level, '123', '531')"/>
			<xsl:for-each select="./Duplicate">
                <!-- Convert the ATC code to uppercase -->
				<xsl:variable name="ATCCode" select="translate(@ATCCode,'abcdefghijklmnopqrstuvwxyz','ABCDEFGHIJKLMNOPQRSTUVWXYZ')"/>
				<xsl:variable name="DupPair">
					<xsl:for-each select="./*">
						<xsl:variable name="type" select="name()"/>
						<xsl:variable name="ref" select="@reference"/>
                        <!-- Lookup the name from the reference -->
						<em><xsl:value-of select="/Result/Interaction/*[@reference=$ref]/@name"/></em>
						<xsl:if test="position()=1">
							<xsl:text> and </xsl:text>
						</xsl:if>
					</xsl:for-each>
				</xsl:variable>

				<xsl:element name="div">
					<xsl:attribute name="id">d_<xsl:value-of select="$DuplicateLevel"/>_<xsl:value-of select="translate($DupPair, 'ABCDEFGHIJKLMNOPQRSTUVWXYZ ','abcdefghijklmnopqrstuvwxyz_')"/>_<xsl:value-of select="translate($DupPair, 'ABCDEFGHIJKLMNOPQRSTUVWXYZ ','abcdefghijklmnopqrstuvwxyz_')"/></xsl:attribute>
					<h3>
						<a>
							<xsl:call-template name="SeverityColor">
								<xsl:with-param name="SeverityLevel" select="$DuplicateLevel"/>
							</xsl:call-template>
							<xsl:value-of select="$DupPair"/> [ATC Code: <xsl:value-of select="$ATCCode"/>]
                        </a>
					</h3>
				</xsl:element>
			</xsl:for-each>
		</xsl:for-each>
	</xsl:template>

    <xsl:template match="DuplicateIngredient">
        <xsl:for-each select="./Warning[contains($DuplicateIngredientFilter, concat('^',@Level, '^'))]">
            <xsl:variable name="DuplicateLevel" select="translate(@Level, '123', '531')"/>
            <xsl:for-each select="./Molecule">
                <xsl:variable name="DupPair">
                    <xsl:for-each select="./*">
                        <xsl:variable name="type" select="name()"/>
                        <xsl:variable name="ref" select="@reference"/>
                        <!-- Lookup the name from the reference -->
                        <em>
                            <xsl:value-of select="/Result/Interaction/*[@reference=$ref]/@name"/>
                        </em>
                        <xsl:if test="position()=1">
                            <xsl:text> and </xsl:text>
                        </xsl:if>
                    </xsl:for-each>
                </xsl:variable>
                <xsl:element name="div">
                    <xsl:attribute name="id">d_
                        <xsl:value-of select="$DuplicateLevel"/>_
                        <xsl:value-of select="translate($DupPair, 'ABCDEFGHIJKLMNOPQRSTUVWXYZ ','abcdefghijklmnopqrstuvwxyz_')"/>_
                        <xsl:value-of select="translate($DupPair, 'ABCDEFGHIJKLMNOPQRSTUVWXYZ ','abcdefghijklmnopqrstuvwxyz_')"/>
                    </xsl:attribute>
                    <h3>
                        <a>
                            <xsl:call-template name="SeverityColor">
                                <xsl:with-param name="SeverityLevel" select="$DuplicateLevel"/>
                            </xsl:call-template>
                            <xsl:value-of select="$DupPair"/> [
                            <xsl:value-of select="@name"/>]
                        
                        </a>
                    </h3>
				</xsl:element>
			</xsl:for-each>
        </xsl:for-each>
    </xsl:template>
	
	<xsl:template match="References">
		<xsl:param name="DivId"/>
		<h4 class="subheading">References</h4>
		<ul>
			<xsl:apply-templates select="JournalReference"/>
			<xsl:apply-templates select="ElectronicReference"/>
			<xsl:apply-templates select="BookReference"/>
			<xsl:apply-templates select="WebReference"/>
		</ul>
	</xsl:template>

	<!-- Need disable-output-escaping="yes" to preserve foreign names -->

	<xsl:template match="JournalReference">
		<li>
			<xsl:value-of select="Author" disable-output-escaping="yes"/>
			<xsl:text>. </xsl:text>
			<xsl:value-of select="Title"/>
			<xsl:text>. </xsl:text>
			<i>
				<xsl:value-of select="Journal"/>
			</i>
			<xsl:text>. </xsl:text>
			<xsl:value-of select="Year"/>
			<xsl:text>; </xsl:text>
			<xsl:value-of select="Volume"/>
			<xsl:if test="string-length(Part) &gt; 0">
				<xsl:text>(</xsl:text>
				<xsl:value-of select="Part"/>
				<xsl:text>)</xsl:text>
			</xsl:if>
			<xsl:text>:</xsl:text>
			<xsl:value-of select="Page"/>
		</li>
	</xsl:template>
	<xsl:template match="ElectronicReference">
		<li>
			<xsl:value-of select="Title"/>
			<xsl:text>. </xsl:text>
			<xsl:value-of select="RefTitle"/>
			<xsl:text>. </xsl:text>
			<xsl:value-of select="SystemName"/>
			<xsl:text>. </xsl:text>
			<xsl:value-of select="SystemAuthor" disable-output-escaping="yes"/>
			<xsl:text>. </xsl:text>
			<xsl:value-of select="SystemManf"/>
			<xsl:text>. </xsl:text>
			<xsl:value-of select="SystemManfLoc"/>
			<xsl:text>. </xsl:text>
			<xsl:value-of select="SystemManfCountry"/>
			<xsl:text>. </xsl:text>
			<xsl:text> Available from URL: </xsl:text>
			<a>
				<xsl:attribute name="href"><xsl:value-of select="URL"/></xsl:attribute>
				<xsl:value-of select="URL"/>
			</a>
			<xsl:text>. (Accessed on </xsl:text>
			<xsl:value-of select="Date"/>
			<xsl:text>)</xsl:text>
		</li>
	</xsl:template>
	<xsl:template match="BookReference">
		<li>
			<xsl:value-of select="Author" disable-output-escaping="yes"/>
			<xsl:text>. </xsl:text>
			<xsl:value-of select="ChapterTitle"/>
			<xsl:text>. In: </xsl:text>
			<xsl:value-of select="Title"/>
			<xsl:text>. </xsl:text>
			<xsl:value-of select="Edition"/>
			<xsl:text> ed. </xsl:text>
			<xsl:if test="string-length(Volume) &gt; 0">
				<xsl:value-of select="Volume"/>
				<xsl:text>. </xsl:text>
			</xsl:if>
			<xsl:value-of select="PublishLoc"/>
			<xsl:text>: </xsl:text>
			<xsl:value-of select="Publisher"/>
			<xsl:text>; </xsl:text>
			<xsl:value-of select="Year"/>
			<xsl:text>; </xsl:text>
			<xsl:value-of select="Page"/>
			<xsl:text>. </xsl:text>
		</li>
	</xsl:template>

	<xsl:template match="WebReference">
		<li>
			<xsl:value-of select="Title"/><xsl:text>. </xsl:text>
				<i><xsl:value-of select="Site"/><xsl:text>. </xsl:text></i>
			<xsl:text> URL: </xsl:text>
			<a>
				<xsl:attribute name="href"><xsl:value-of select="URL"/></xsl:attribute>
				<xsl:value-of select="URL"/>
			</a>
			<xsl:text>. (Accessed on </xsl:text>
			<xsl:value-of select="Date"/>
			<xsl:text>)</xsl:text>
		</li>
	</xsl:template>


	<xsl:template name="SeverityColor">
		<xsl:param name="SeverityLevel"/>
		<table class="severity">
			<tr>
				<xsl:element name="td">
					<xsl:attribute name="class"><xsl:value-of select="concat('severity', $SeverityLevel)"/></xsl:attribute>
				</xsl:element>
			</tr>
		</table>&#160;
	</xsl:template>

	<xsl:template name="d2dSeverity">
		<xsl:param name="Severity"/>
		<h4 class="subheading">Severity Level</h4>
		<p><em><xsl:value-of select="$Severity"/></em><xsl:text> - </xsl:text><xsl:apply-templates select="$d2d_sev_level-top"><xsl:with-param name="SeverityLevel" select="$Severity"/></xsl:apply-templates></p>
	</xsl:template>

	<xsl:template match="mims:d2d_sev_levels">
		<xsl:param name="SeverityLevel"/>
		<xsl:value-of select="key('d2d_sev_level-lookup', $SeverityLevel)/text()"/>
	</xsl:template>


	<xsl:template name="d2hSeverity">
		<xsl:param name="Severity"/>
		<h4 class="subheading">Severity Level</h4>
		<p><em><xsl:value-of select="$Severity"/></em><xsl:text> - </xsl:text><xsl:apply-templates select="$d2h_sev_level-top"><xsl:with-param name="SeverityLevel" select="$Severity"/></xsl:apply-templates></p>
	</xsl:template>

	<xsl:template match="mims:d2l_sev_levels">
		<xsl:param name="SeverityLevel"/>
		<xsl:value-of select="key('d2l_sev_level-lookup', $SeverityLevel)/text()"/>
	</xsl:template>

	<xsl:template name="d2lSeverity">
		<xsl:param name="Severity"/>
		<h4 class="subheading">Severity Level</h4>
		<em>
			<xsl:value-of select="$Severity"/>
		</em>
		<xsl:text> - </xsl:text>
		<xsl:apply-templates select="$d2l_sev_level-top">
			<xsl:with-param name="SeverityLevel" select="$Severity"/>
		</xsl:apply-templates>
	</xsl:template>

	<xsl:template match="mims:d2h_sev_levels">
		<xsl:param name="SeverityLevel"/>
		<xsl:value-of select="key('d2h_sev_level-lookup', $SeverityLevel)/text()"/>
	</xsl:template>

	<xsl:template name="docLevel">
		<xsl:param name="DocLevel"/>
		<h4 class="subheading">Documentation Level</h4>
		<p><em><xsl:value-of select="$DocLevel"/></em>
		<xsl:text> - </xsl:text>
		<xsl:apply-templates select="$doc_level-top">
			<xsl:with-param name="DocLevel" select="$DocLevel"/>
		</xsl:apply-templates></p>
	</xsl:template>

	<xsl:template match="mims:doc_levels">
		<xsl:param name="DocLevel"/>
		<xsl:value-of select="key('doc_level-lookup', $DocLevel)/text()"/>
	</xsl:template>

	<xsl:template match="mims:preg_cats">
		<xsl:param name="Source"/>
		<xsl:param name="Cat"/>
		<xsl:value-of select="key('preg_cat-lookup', concat($Source, '!', $Cat))/text()"/>
	</xsl:template>
	<xsl:template match="/Result/Detail/Product">
		<STYLE>
   BODY {COLOR: #6B696B; FONT-FAMILY: Tahoma;FONT-SIZE: 9pt;} 
   TR.clsOdd { background-Color: beige; } 
   TR.clsEven { background-color: #cccccc; }
   TD.FirstColumn{ background-Color: beige;FONT-FAMILY: Tahoma; FONT-SIZE: 9pt;COLOR:black; } 
     TR.Header{ background-Color: beige;FONT-FAMILY: Tahoma; FONT-SIZE: 9pt;COLOR:black; } 
     table{FONT-FAMILY:Tahoma; FONT-SIZE:10pt;COLOR: #6B696B;}
</STYLE>
		<p align="center">
			<strong>
				<xsl:value-of select="@name"/>
			</strong>
			<br/>
			<xsl:if test="string-length(GGPI/@name) &gt;  0">
				<xsl:text>(</xsl:text>
				<xsl:value-of select="GGPI/@name"/>
				<xsl:text>)</xsl:text>
			</xsl:if>
			<br/>
		</p>
		<table width="100%">
			<tr>
				<td/>
				<td colspan="5">				
				</td>
			</tr>
			<tr>
				<td>
					<xsl:text> </xsl:text>
				</td>
			</tr>
			<xsl:if test="string-length(@code) &gt;  0">
				<tr>
					<td class="FirstColumn">Code</td>
					<td>
						<xsl:value-of select="@code"/>
					</td>
				</tr>
			</xsl:if>
			<xsl:if test="string-length(@registrationDate) &gt;  0">
				<tr>
					<td class="FirstColumn">Registration Date</td>
					<td>
						<xsl:value-of select="@registrationDate"/>
					</td>
				</tr>
			</xsl:if>
			<xsl:if test="string-length(@onMarketDate) &gt;  0">
				<tr>
					<td class="FirstColumn">On Market Date</td>
					<td>
						<xsl:value-of select="@onMarketDate"/>
					</td>
				</tr>
			</xsl:if>
			<xsl:if test="string-length(@offMarketDate) &gt;  0">
				<tr>
					<td class="FirstColumn">Off Market Date</td>
					<td>
						<xsl:value-of select="@offMarketDate"/>
					</td>
				</tr>
			</xsl:if>
			<xsl:if test="string-length(@hardStop) &gt;  0">
				<tr>
					<td class="FirstColumn">Hard Stop</td>
					<td>
						<xsl:value-of select="@hardStop"/>
					</td>
				</tr>
			</xsl:if>
			<xsl:for-each select="Items/Item">
				<xsl:for-each select="Routes/Route">
					<xsl:if test="string-length(@name) &gt;  0">
						<tr>
							<td class="FirstColumn">Route</td>
							<td>
								<xsl:value-of select="@name"/>
							</td>
						</tr>
					</xsl:if>
				</xsl:for-each>
				<xsl:if test="string-length(Form/@name) &gt;  0">
					<tr>
						<td class="FirstColumn">Form</td>
						<td>
							<xsl:value-of select="Form/@name"/>
						</td>
					</tr>
				</xsl:if>
				<xsl:if test="string-length(ActiveCompositionGroup/GenericItem/HL7Form/@name) &gt;  0">
					<tr>
						<td class="FirstColumn">HL7 Form</td>
						<td>
							<xsl:value-of select="ActiveCompositionGroup/GenericItem/HL7Form/@name"/>
						</td>
					</tr>
				</xsl:if>
				<tr>
					<td class="FirstColumn">Molecule</td>
					<td>
						<table>
							<tr class="Header">
								<td>name</td>
								<td>strength</td>
								<td>unit</td>
								<xsl:if test="string-length(ActiveCompositionGroup/ActiveComposition/Molecules/Molecul/@perVolume) &gt;  0">
									<td>per Volume</td>
								</xsl:if>
								<xsl:if test="string-length(ActiveCompositionGroup/ActiveComposition/Molecules/Molecul/@perVolumeUnit) &gt;  0">
									<td>Unit</td>
								</xsl:if>
							</tr>
							<xsl:for-each select="ActiveCompositionGroup/ActiveComposition/Molecules/Molecule">
								<tr>
									<td>
										<xsl:value-of select="@name"/>
									</td>
									<td>
										<xsl:value-of select="@strength"/>
									</td>
									<td>
										<xsl:value-of select="@strengthUnit"/>
									</td>
									<xsl:if test="string-length(ActiveCompositionGroup/ActiveComposition/Molecules/Molecul/@perVolume) &gt;  0">
										<td>
											<xsl:value-of select="@perVolume"/>
										</td>
									</xsl:if>
									<xsl:if test="string-length(ActiveCompositionGroup/ActiveComposition/Molecules/Molecul/@perVolumeUnit) &gt;  0">
										<td>
											<xsl:value-of select="@perVolumeUnit"/>
										</td>
									</xsl:if>
								</tr>
							</xsl:for-each>
						</table>
					</td>
				</tr>
				<xsl:if test="string-length(ActiveCompositionGroup/GenericItem/@name) &gt;  0">
					<tr>
						<td class="FirstColumn">Generic Name</td>
						<td>
							<xsl:value-of select="ActiveCompositionGroup/GenericItem/@name"/>
						</td>
					</tr>
				</xsl:if>
				<xsl:if test="Images/@count &gt;  0">
					<tr>
						<td class="FirstColumn">Image</td>
						<td>
							<img alt="PIC" src="C:\FTImage.gif"/>
						</td>
					</tr>
				</xsl:if>
			</xsl:for-each>
			<tr>
				<td class="FirstColumn">Therapeutic Class</td>
				<td>
					<ui>
						<xsl:for-each select="TherapeuticClasses/TherapeuticClass">
							<li>
								<xsl:value-of select="@name"/>
							</li>
						</xsl:for-each>
					</ui>
				</td>
			</tr>
			<xsl:if test="ATCCodes/@count &gt;  0">
				<tr>
					<td class="FirstColumn">ATC Code</td>
					<td>
						<table width="100%">
							<tr class="Header">
								<td class="FirstColumn" width="70%">name</td>
								<td class="FirstColumn">code</td>
							</tr>
							
							<xsl:for-each select="ATCCodes/ATCCode">
								<tr>
									<td>
										<xsl:value-of select="@name"/>
									</td>
									<td>
										<xsl:value-of select="@code"/>
									</td>
								</tr>
							</xsl:for-each>
							<xsl:for-each select="ATCCodes/AtcClassification">
								<tr>
									<td>
										<xsl:value-of select="@name"/>
									</td>
									<td>
										<xsl:value-of select="@code"/>
									</td>
								</tr>
							</xsl:for-each>
						</table>
					</td>
				</tr>
			</xsl:if>
			<tr>
				<td class="FirstColumn">Company</td>
				<td>
					<table width="100%">
						<tr class="Header">
							<td class="FirstColumn" width="70%">name</td>
							<td class="FirstColumn">Type</td>
						</tr>
						<xsl:for-each select="Companies/Company">
							<tr>
								<td>
									<xsl:value-of select="@name"/>
								</td>
								<td>
									<xsl:value-of select="CompanyType/@name"/>
								</td>
							</tr>
						</xsl:for-each>
					</table>
				</td>
			</tr>
		</table>
	</xsl:template>
	<xsl:template match="mims:preg_pics">
		<xsl:param name="Source"/>
		<xsl:param name="Cat"/>
		<xsl:value-of select="key('preg_pic-lookup', concat($Source, '!', $Cat))/text()"/>
	</xsl:template>

    <!-- Lookup tables -->
	<mims:d2d_sev_levels>
		<mims:d2d_sev_level name="Caution">
The interaction may occur based on the mechanism of action of the co-administered medicines. Be alert for increased or decreased effect, depending on the combination of medicines.
        </mims:d2d_sev_level>
		<mims:d2d_sev_level name="Minor">
Clinical effects of the interaction are limited and may be bothersome but would not usually require a major change to therapy. The patient
should be monitored for the possible manifestations of the interaction.
        </mims:d2d_sev_level>
		<mims:d2d_sev_level name="Moderate">
These medications may interact resulting in the potential deterioration of the patient's condition. The patient should be monitored for the possible manifestations of the interaction. Medical intervention or a change in therapy may be required.
        </mims:d2d_sev_level>
		<mims:d2d_sev_level name="Severe">
The interaction between these medications may be life-threatening or may cause permanent damage. These medications are not usually used concurrently; medical intervention may be required.
        </mims:d2d_sev_level>
	</mims:d2d_sev_levels>

	<mims:d2h_sev_levels>
		<mims:d2h_sev_level name="Contraindicated">
Absolute contraindication; the use of the drug is completely inadvisable and should be avoided.</mims:d2h_sev_level>
		<mims:d2h_sev_level name="Contraindication">
Absolute contraindication; the use of the drug is completely inadvisable and should be avoided.</mims:d2h_sev_level>
		<mims:d2h_sev_level name="Extreme Caution">
Use of the drug is not recommended and the patient has a higher risk of developing adverse effects or exacerbation of their existing health condition, however, use is still possible and the risk may be outweighed by other considerations.</mims:d2h_sev_level>
	</mims:d2h_sev_levels>

	<mims:d2l_sev_levels>
		<mims:d2l_sev_level name="Contraindicated">
Human experience may or may not be available but these drugs can have severe toxicity to a nursing infant. The drug should not be given to a nursing mother. These drugs also include those that are indicated in conditions where lactation is a contraindication.</mims:d2l_sev_level>
		<mims:d2l_sev_level name="Contraindication">
Human experience may or may not be available but these drugs can have severe toxicity to a nursing infant. The drug should not be given to a nursing mother. These drugs also include those that are indicated in conditions where lactation is a contraindication.</mims:d2l_sev_level>
		<mims:d2l_sev_level name="Avoid if possible">
These drugs have been reported to cause clinically significant side effects to a nursing infant, and/or existing animal data suggest significant risks to a nursing infant. These agents may also inhibit lactation or may harm the mother. If use during breastfeeding is unavoidable, monitoring is needed for possible toxicity to either mother or infant.
</mims:d2l_sev_level>
		<mims:d2l_sev_level name="Caution">
These drugs may theoretically cause side effects to a nursing infant but have either not been observed to do so or have only occasionally caused mild side effects. These agents may be given to a lactating mother but monitoring is needed for possible toxicity to either mother or infant.</mims:d2l_sev_level>
	</mims:d2l_sev_levels>

	<mims:doc_levels>
		<mims:doc_level name="Well Established">
There have been several published reports of this interaction. The pharmacological explanation of why the interaction occurs is well documented and understood.There are usually controlled studies that have established that the interaction exists.
        </mims:doc_level>
		<mims:doc_level name="Good">
Although controlled studies may not have been performed, several case reports have been documented and other data strongly suggests this interaction exists.
        </mims:doc_level>
		<mims:doc_level name="Limited">
Few reports of this interaction exist. These few reports usually consist of limited case reports where clinically sound justification of the interaction is found.
        </mims:doc_level>
		<mims:doc_level name="Not Established">
The interaction may have occurred with other medicines within the same class, or there is a theoretical possibility that the interaction exists.
        </mims:doc_level>
	</mims:doc_levels>

	<mims:preg_cats>
		<mims:preg_cat cat="FDA!A">Adequate and well-controlled human studies have failed to demonstrate a risk to the fetus in the first trimester of pregnancy (and there is no evidence of risk in later trimesters).</mims:preg_cat>
		<mims:preg_cat cat="FDA!B">Animal reproduction studies have failed to demonstrate a risk to the fetus and there are no adequate and well-controlled studies in pregnant women OR Animal studies have shown an adverse effect, but adequate and well-controlled studies in pregnant women have failed to demonstrate a risk to the fetus in any trimester.</mims:preg_cat>
		<mims:preg_cat cat="FDA!C">Animal reproduction studies have shown an adverse effect on the fetus and there are no adequate and well-controlled studies in humans, but potential benefits may warrant use of the drug in pregnant women despite potential risks.</mims:preg_cat>
		<mims:preg_cat cat="FDA!D">There is positive evidence of human fetal risk based on adverse reaction data from investigational or marketing experience or studies in humans, but potential benefits may warrant use of the drug in pregnant women despite potential risks.</mims:preg_cat>
		<mims:preg_cat cat="FDA!X">Studies in animals or humans have demonstrated fetal abnormalities and/or there is positive evidence of human fetal risk based on adverse reaction data from investigational or marketing experience, and the risks involved in use of the drug in pregnant women clearly outweigh potential benefits.</mims:preg_cat>

		<mims:preg_cat cat="ADEC!A">Drugs which have been taken by a large number of pregnant women and women of childbearing age without an increase in the frequency of malformations or other direct or indirect harmful effects on the fetus having been observed.</mims:preg_cat>
		<mims:preg_cat cat="ADEC!B1">Drugs which have been taken by only a limited number of pregnant women and women of childbearing age, without an increase in the frequency of malformation or other direct or indirect harmful effects on the human fetus having been observed.
Studies in animals have not shown evidence of an increased occurrence of fetal damage.</mims:preg_cat>
		<mims:preg_cat cat="ADEC!B2">Drugs which have been taken by only a limited number of pregnant women and women of childbearing age, without an increase in the frequency of malformation or other direct or indirect harmful effects on the human fetus having been observed.
Studies in animals are inadequate or may be lacking, but available data show no evidence of an increased occurrence of fetal damage.</mims:preg_cat>
		<mims:preg_cat cat="ADEC!B3">Drugs which have been taken by only a limited number of pregnant women and women of childbearing age, without an increase in the frequency of malformation or other direct or indirect harmful effects on the human fetus having been observed.
Studies in animals have shown evidence of an increased occurrence of fetal damage, the significance of which is considered uncertain in humans.</mims:preg_cat>
		<mims:preg_cat cat="ADEC!C">Drugs which, owing to their pharmaceutical effects, have caused or may be suspected of causing, harmful effects on the human fetus or neonate without causing malformations. These effects may be reversible.</mims:preg_cat>
		<mims:preg_cat cat="ADEC!D">Drugs which have caused, are suspected to have caused or may be expected to cause, an increased incidence of human fetal malformations or irreversible damage. These drugs may also have adverse pharmacological effects.</mims:preg_cat>
		<mims:preg_cat cat="ADEC!X">Drugs that have such a high risk of causing permanent damage to the fetus that they should NOT be used in pregnancy or when there is a possibility of pregnancy.</mims:preg_cat>
	</mims:preg_cats>

	<mims:preg_pics>
		<mims:preg_pic cat="FDA!A">images/cata.gif</mims:preg_pic>
		<mims:preg_pic cat="FDA!B">images/catb.gif</mims:preg_pic>
		<mims:preg_pic cat="FDA!C">images/catc.gif</mims:preg_pic>
		<mims:preg_pic cat="FDA!D">images/catd.gif</mims:preg_pic>
		<mims:preg_pic cat="FDA!X">images/catx.gif</mims:preg_pic>
	</mims:preg_pics>

	<xsl:template match="Content">
		<xsl:choose>
			<xsl:when test="count(*)=0">
				<div class="ui-widget">
					<div class="ui-state-default ui-corner-all warning-bar">
				<p><span class="ui-icon ui-icon-alert"></span>No prescribing information available.</p>
					</div>
				</div>
			</xsl:when>
			<xsl:otherwise>
		<div id="tabs">
			<ul>
				<xsl:if test="//BRIEFMONO">
					<li>
						<a href="#tab-brief">Abbreviated</a>
					</li>
				</xsl:if>
				<xsl:if test="//FULLMONO">
					<li>
						<a href="#tab-full">Full</a>
					</li>
				</xsl:if>
				<xsl:if test="//MONOGRAPH">
					<li>
						<a href="#tab-generic">Generic</a>
					</li>
				</xsl:if>
			</ul>
			<xsl:if test="//FULLMONO">
				<div id="tab-full">
					<xsl:apply-templates select="//FULLMONO"/>
				</div>
			</xsl:if>
			<xsl:if test="//BRIEFMONO">
				<div id="tab-brief">
					<xsl:apply-templates select="//BRIEFMONO"/>
				</div>
			</xsl:if>
			<xsl:if test="//MONOGRAPH">
				<div id="tab-generic">
					<xsl:apply-templates select="//MONOGRAPH"/>
				</div>
			</xsl:if>
		</div>
		</xsl:otherwise>
		</xsl:choose>
	</xsl:template>

<!-- For Monograph -->
	<xsl:template match="BRIEFMONO">
		<h4 class="subheading"><xsl:value-of select="../@name"/></h4>
		<table class="ui-widget monograph">

			<xsl:call-template name="ShowSection">
				<xsl:with-param name="SectionName">Contents</xsl:with-param>
				<xsl:with-param name="SectionData" select="BC"/>
			</xsl:call-template>

			<xsl:call-template name="ShowSection">
				<xsl:with-param name="SectionName">Indications</xsl:with-param>
				<xsl:with-param name="SectionData" select="BI"/>
			</xsl:call-template>

			<xsl:call-template name="ShowSection">
				<xsl:with-param name="SectionName">Dosage</xsl:with-param>
				<xsl:with-param name="SectionData" select="BD"/>
			</xsl:call-template>

			<xsl:call-template name="ShowSection">
				<xsl:with-param name="SectionName">Contraindications</xsl:with-param>
				<xsl:with-param name="SectionData" select="BCI"/>
			</xsl:call-template>

			<xsl:call-template name="ShowSection">
				<xsl:with-param name="SectionName">Special Precautions</xsl:with-param>
				<xsl:with-param name="SectionData" select="BSP"/>
			</xsl:call-template>

			<xsl:call-template name="ShowSection">
				<xsl:with-param name="SectionName">Adverse Reactions</xsl:with-param>
				<xsl:with-param name="SectionData" select="BAR"/>
			</xsl:call-template>

			<xsl:call-template name="ShowSection">
				<xsl:with-param name="SectionName">Drug Interactions</xsl:with-param>
				<xsl:with-param name="SectionData" select="BDI"/>
			</xsl:call-template>

			<xsl:call-template name="ShowSection">
				<xsl:with-param name="SectionName">ATC Classification</xsl:with-param>
				<xsl:with-param name="SectionData" select="ATCDET"/>
			</xsl:call-template>

			<xsl:call-template name="ShowSection">
				<xsl:with-param name="SectionName">Poison Schedule</xsl:with-param>
				<xsl:with-param name="SectionData" select="BPOI"/>
			</xsl:call-template>

			<xsl:call-template name="ShowSection">
				<xsl:with-param name="SectionName">Packing/Presentation</xsl:with-param>
				<xsl:with-param name="SectionData" select="BPP"/>
			</xsl:call-template>

		</table>
	</xsl:template>

	<xsl:template match="FULLMONO">
		<h4 class="subheading"><xsl:value-of select="../@name"/></h4>
		<table class="ui-widget monograph">
			<xsl:call-template name="ShowSection">
				<xsl:with-param name="SectionName">Contents</xsl:with-param>
				<xsl:with-param name="SectionData" select="FC"/>
			</xsl:call-template>

			<xsl:call-template name="ShowSection">
				<xsl:with-param name="SectionName">Description</xsl:with-param>
				<xsl:with-param name="SectionData" select="FDESC"/>
			</xsl:call-template>

			<xsl:call-template name="ShowSection">
				<xsl:with-param name="SectionName">Indications</xsl:with-param>
				<xsl:with-param name="SectionData" select="FI"/>
			</xsl:call-template>

			<xsl:call-template name="ShowSection">
				<xsl:with-param name="SectionName">Dosage</xsl:with-param>
				<xsl:with-param name="SectionData" select="FD"/>
			</xsl:call-template>

			<xsl:call-template name="ShowSection">
				<xsl:with-param name="SectionName">OverDosage</xsl:with-param>
				<xsl:with-param name="SectionData" select="FOD"/>
			</xsl:call-template>

			<xsl:call-template name="ShowSection">
				<xsl:with-param name="SectionName">Contraindications</xsl:with-param>
				<xsl:with-param name="SectionData" select="FCI"/>
			</xsl:call-template>

			<xsl:call-template name="ShowSection">
				<xsl:with-param name="SectionName">Special precautions</xsl:with-param>
				<xsl:with-param name="SectionData" select="FSP"/>
			</xsl:call-template>

			<xsl:call-template name="ShowSection">
				<xsl:with-param name="SectionName">Adverse Drug Reactions</xsl:with-param>
				<xsl:with-param name="SectionData" select="FAR"/>
			</xsl:call-template>

			<xsl:call-template name="ShowSection">
				<xsl:with-param name="SectionName">Side effects</xsl:with-param>
				<xsl:with-param name="SectionData" select="FSE"/>
			</xsl:call-template>

			<xsl:call-template name="ShowSection">
				<xsl:with-param name="SectionName">Drug Interactions</xsl:with-param>
				<xsl:with-param name="SectionData" select="FDI"/>
			</xsl:call-template>

			<xsl:call-template name="ShowSection">
				<xsl:with-param name="SectionName">Mechanism of Action</xsl:with-param>
				<xsl:with-param name="SectionData" select="FA"/>
			</xsl:call-template>

			<xsl:call-template name="ShowSection">
				<xsl:with-param name="SectionName">Caution</xsl:with-param>
				<xsl:with-param name="SectionData" select="FCAU"/>
			</xsl:call-template>

			<xsl:call-template name="ShowSection">
				<xsl:with-param name="SectionName">ATC Classification</xsl:with-param>
				<xsl:with-param name="SectionData" select="ATCDET"/>
			</xsl:call-template>

			<xsl:call-template name="ShowSection">
				<xsl:with-param name="SectionName">Poison Schedule</xsl:with-param>
				<xsl:with-param name="SectionData" select="FPOI"/>
			</xsl:call-template>

			<xsl:call-template name="ShowSection">
				<xsl:with-param name="SectionName">Storage</xsl:with-param>
				<xsl:with-param name="SectionData" select="FSTO"/>
			</xsl:call-template>

			<xsl:call-template name="ShowSection">
				<xsl:with-param name="SectionName">Packing/Presentation</xsl:with-param>
				<xsl:with-param name="SectionData" select="FPP"/>
			</xsl:call-template>
		</table>
	</xsl:template>

	<xsl:template match="MONOGRAPH">
		<h4 class="subheading"><xsl:value-of select="../@name"/></h4>
		<table class="ui-widget monograph">
			<xsl:call-template name="ShowSection">
				<xsl:with-param name="SectionName">Contents</xsl:with-param>
				<xsl:with-param name="SectionData" select="GENMONO"/>
			</xsl:call-template>

			<xsl:call-template name="ShowSection">
				<xsl:with-param name="SectionName">Indications</xsl:with-param>
				<xsl:with-param name="SectionData" select="BI"/>
			</xsl:call-template>

			<xsl:call-template name="ShowSection">
				<xsl:with-param name="SectionName">Dosage</xsl:with-param>
				<xsl:with-param name="SectionData" select="GDOSE"/>
			</xsl:call-template>

			<xsl:call-template name="ShowSection">
				<xsl:with-param name="SectionName">Administration</xsl:with-param>
				<xsl:with-param name="SectionData" select="GPPPA"/>
			</xsl:call-template>

			<xsl:call-template name="ShowSection">
				<xsl:with-param name="SectionName">Contraindications</xsl:with-param>
				<xsl:with-param name="SectionData" select="GCI"/>
			</xsl:call-template>

			<xsl:call-template name="ShowSection">
				<xsl:with-param name="SectionName">Special precautions</xsl:with-param>
				<xsl:with-param name="SectionData" select="GSP"/>
			</xsl:call-template>

			<xsl:call-template name="ShowSection">
				<xsl:with-param name="SectionName">Adverse reactions</xsl:with-param>
				<xsl:with-param name="SectionData" select="GAR"/>
			</xsl:call-template>

			<xsl:call-template name="ShowSection">
				<xsl:with-param name="SectionName">Drug interactions</xsl:with-param>
				<xsl:with-param name="SectionData" select="GDI"/>
			</xsl:call-template>

			<xsl:call-template name="ShowSection">
				<xsl:with-param name="SectionName">Mechanism of action</xsl:with-param>
				<xsl:with-param name="SectionData" select="GACTION"/>
			</xsl:call-template>

			<xsl:call-template name="ShowSection">
				<xsl:with-param name="SectionName">Lab Interference</xsl:with-param>
				<xsl:with-param name="SectionData" select="GLAB"/>
			</xsl:call-template>

			<xsl:call-template name="ShowSection">
				<xsl:with-param name="SectionName">Food Interaction</xsl:with-param>
				<xsl:with-param name="SectionData" select="GFOOD"/>
			</xsl:call-template>

			<xsl:call-template name="ShowSection">
				<xsl:with-param name="SectionName">MIMS Class</xsl:with-param>
				<xsl:with-param name="SectionData" select="translate(GCLS, '*', '')"/>
			</xsl:call-template>

			<xsl:call-template name="ShowSection">
				<xsl:with-param name="SectionName">ATC Classification</xsl:with-param>
				<xsl:with-param name="SectionData" select="GATC2"/>
			</xsl:call-template>

			<xsl:call-template name="ShowSection">
				<xsl:with-param name="SectionName">Poison Schedule</xsl:with-param>
				<xsl:with-param name="SectionData" select="BPOI"/>
			</xsl:call-template>

			<xsl:call-template name="ShowSection">
				<xsl:with-param name="SectionName">Pregnancy Category</xsl:with-param>
				<xsl:with-param name="SectionData" select="BPCAT"/>
			</xsl:call-template>

		</table>
	</xsl:template>

	<xsl:template name="ShowSection">
		<xsl:param name="SectionName"/>
		<xsl:param name="SectionData"/>
		<xsl:if test="normalize-space($SectionData)">
			<tr>
				<td class="monograph-heading ui-widget-content">
					<span style="font-weight: bold">
						<xsl:value-of select="$SectionName"/>
					</span>
				</td>
				<td class="ui-widget-content">
					<xsl:value-of select="$SectionData" disable-output-escaping="yes"/>
				</td>
			</tr>
		</xsl:if>
	</xsl:template>

<xsl:template match="DrugItem|Product|GenericItem|SpecificItem|GGPI|ActiveComposition|ActiveCompositionGroup" mode="label">
<xsl:variable name="IntType1" select="name()"/>
<xsl:variable name="IntProd1" select="@name"/>
<xsl:variable name="IntRef1" select="@reference"/>
<div>
<h3><a href="#"><xsl:value-of select="$IntProd1"/></a></h3>
<div>
<div class="drug-label">
	<h4><xsl:value-of select="$IntProd1"/></h4>
	<xsl:apply-templates select=".//CautionaryLabel"/>
</div>
<xsl:apply-templates select=".//CautionaryLabel" mode="cals-details"/>

</div>
</div>
</xsl:template>

<xsl:template match="CautionaryLabel">
	<p><xsl:value-of select="Warning"/></p>
</xsl:template>

<xsl:template match="CautionaryLabel" mode="cals-details">
	<div class="cal-details">
	<p><strong><xsl:value-of select="@type"/> Warning:</strong> <xsl:value-of select="Warning"/></p>
	<xsl:if test="Description">
<p><strong>Explanation:</strong> <xsl:value-of select="Description"/></p>
</xsl:if>
<xsl:apply-templates select="References" mode="cals"/>
<hr/>
</div>
</xsl:template>

<xsl:template match="References" mode="cals">
	<h4 class="subheading">References</h4>
	<ul>
		<xsl:apply-templates select="Journal" mode="cals"/>
		<xsl:apply-templates select="Website" mode="cals"/>
	</ul>
</xsl:template>

<xsl:template match="Website|Journal" mode="cals">
	<li>
		<xsl:value-of select="Title"/><xsl:text>. </xsl:text>
		<xsl:choose>
			<xsl:when test="URL">
		<xsl:element name="a">
			<xsl:attribute name="href"><xsl:value-of select="URL"/></xsl:attribute>
		<i><xsl:value-of select="ReferenceDisplay"/></i><xsl:text>. </xsl:text>

</xsl:element>
	</xsl:when>
	<xsl:otherwise>
<i><xsl:value-of select="ReferenceDisplay"/></i><xsl:text>. </xsl:text>
	</xsl:otherwise>
</xsl:choose>
		<!--<xsl:value-of select="URL"/>-->
	</li>
</xsl:template>

</xsl:stylesheet>
